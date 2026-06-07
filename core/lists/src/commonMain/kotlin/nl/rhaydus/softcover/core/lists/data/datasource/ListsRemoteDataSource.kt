package nl.rhaydus.softcover.core.lists.data.datasource

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import kotlinx.coroutines.withContext
import nl.rhaydus.softcover.CreateListBookMutation
import nl.rhaydus.softcover.CreateListBookMutation.Data.Insert_list_book.List_book.Companion.listBookFragment as createListBookListBookFragment
import nl.rhaydus.softcover.CreateListMutation
import nl.rhaydus.softcover.CreateListMutation.Data.Insert_list.List.Companion.listFragment as createListListFragment
import nl.rhaydus.softcover.GetUserBookListsQuery
import nl.rhaydus.softcover.GetUserBookListsQuery.Data.Me.List.Companion.listFragment as userListsListFragment
import nl.rhaydus.softcover.MarkEditionAsOwnedMutation
import nl.rhaydus.softcover.MarkEditionAsOwnedMutation.Data.Edition_owned.List_book.Companion.listBookFragment
import nl.rhaydus.softcover.RemoveListBookMutation
import nl.rhaydus.softcover.RemoveListBookMutation.Data.Delete_list_book.List.Companion.listFragment as removeListBookListFragment
import nl.rhaydus.softcover.UpdateListBookPositionsMutation
import nl.rhaydus.softcover.UpdateListMutation
import nl.rhaydus.softcover.UpdateListMutation.Data.ListResponse.List.Companion.listFragment as updateListListFragment
import nl.rhaydus.softcover.core.domain.model.AppDispatchers
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.softcover.core.lists.data.mapper.toBookList
import nl.rhaydus.softcover.core.lists.data.mapper.toListBook
import nl.rhaydus.softcover.core.lists.domain.exception.ListNameTakenException
import nl.rhaydus.softcover.core.network.helper.safeMutation
import nl.rhaydus.softcover.core.network.helper.safeQuery
import nl.rhaydus.softcover.type.Int_comparison_exp
import nl.rhaydus.softcover.type.ListBookInput
import nl.rhaydus.softcover.type.ListInput
import nl.rhaydus.softcover.type.List_books_bool_exp
import nl.rhaydus.softcover.type.List_books_set_input
import nl.rhaydus.softcover.type.List_books_updates
import nl.rhaydus.softcover.type.Lists_bool_exp

interface ListsRemoteDataSource {
    suspend fun fetchUserLists(
        userId: Int,
        listIds: Set<Int>? = null,
    ): List<BookList>

    suspend fun createList(name: String): BookList

    suspend fun markEditionAsOwned(edition: BookEdition): ListBook

    suspend fun addBookToList(
        listId: Int,
        bookId: Int,
        editionId: Int,
    ): ListBook

    suspend fun removeListBook(book: ListBook): BookList

    /**
     * Rewrites a contiguous slice of [listId]'s `list_books.position` column: clears positions
     * `startPosition..startPosition + orderedListBookIds.size - 1` server-side, then assigns the
     * new positions to [orderedListBookIds] in order. Mirrors the two-step pattern Hardcover's
     * web client uses (null-then-set) so the server-side unique-position constraint never sees
     * two rows claiming the same slot mid-mutation.
     */
    suspend fun updateListBookPositions(
        listId: Int,
        startPosition: Int,
        orderedListBookIds: List<Int>,
    )

    /**
     * Toggles a custom list's `ranked` flag on Hardcover. Returns the refreshed [BookList] so
     * callers can reconcile any other server-side fields the mutation response carries.
     */
    suspend fun setListRanked(
        listId: Int,
        ranked: Boolean,
    ): BookList
}

private const val DEFAULT_LIST_VIEW: String = "card"
private const val NAME_TAKEN_MARKER: String = "already been taken"

internal class ListsRemoteDataSourceImpl(
    private val apolloClient: ApolloClient,
    private val appDispatchers: AppDispatchers,
) : ListsRemoteDataSource {
    override suspend fun fetchUserLists(
        userId: Int,
        listIds: Set<Int>?,
    ): List<BookList> = withContext(appDispatchers.io) {
        val whereFilter: Optional<Lists_bool_exp?> = if (listIds == null) {
            Optional.Absent
        } else {
            Optional.Present(
                Lists_bool_exp(
                    id = Optional.Present(Int_comparison_exp(_in = Optional.Present(listIds.toList()))),
                ),
            )
        }

        val result = apolloClient.safeQuery(
            GetUserBookListsQuery(where = whereFilter),
        )

        val lists = result.me.firstOrNull()?.lists
            ?: throw Exception("No lists were found")

        lists.mapNotNull { list ->
            list.userListsListFragment()?.toBookList()
        }
    }

    override suspend fun createList(name: String): BookList {
        val input = ListInput(
            name = Optional.Present(name),
            description = Optional.Present(""),
            privacy_setting_id = Optional.Present(PrivacySetting.PUBLIC.code),
            ranked = Optional.Present(false),
            default_view = Optional.Present(DEFAULT_LIST_VIEW),
            url = Optional.Present(""),
        )

        val response = apolloClient
            .safeMutation(mutation = CreateListMutation(list = input))
            .insert_list
            ?: throw Exception("Did not receive a create-list response")

        val errorText: String? = response.errors?.takeIf { it.isNotBlank() }

        if (errorText != null) {
            if (errorText.lowercase().contains(NAME_TAKEN_MARKER)) {
                throw ListNameTakenException(name = name)
            }

            throw Exception("CreateList rejected by server: $errorText")
        }

        val listFragment = response
            .list
            ?.createListListFragment()
            ?: throw Exception("Did not receive a list fragment")

        return listFragment.toBookList()
    }

    override suspend fun markEditionAsOwned(edition: BookEdition): ListBook {
        val mutation = MarkEditionAsOwnedMutation(id = edition.id)

        val listBookFragment = apolloClient
            .safeMutation(mutation = mutation)
            .edition_owned
            ?.list_book
            ?.listBookFragment()
            ?: throw Exception("Did not receive a list book fragment")

        return listBookFragment.toListBook()
            ?: throw Exception("List book mapping resulted in null")
    }

    override suspend fun addBookToList(
        listId: Int,
        bookId: Int,
        editionId: Int,
    ): ListBook {
        val input = ListBookInput(
            book_id = bookId,
            list_id = listId,
            edition_id = Optional.Present(editionId),
            position = Optional.Absent,
        )

        val listBookFragment = apolloClient
            .safeMutation(mutation = CreateListBookMutation(listBook = input))
            .insert_list_book
            ?.list_book
            ?.createListBookListBookFragment()
            ?: throw Exception("Did not receive a list book fragment")

        return listBookFragment.toListBook()
            ?: throw Exception("List book mapping resulted in null")
    }

    override suspend fun removeListBook(book: ListBook): BookList {
        val mutation = RemoveListBookMutation(id = book.listBookId)

        val listFragment = apolloClient
            .safeMutation(mutation = mutation)
            .delete_list_book
            ?.list
            ?.removeListBookListFragment()
            ?: throw Exception("Did not receive a list fragment")

        return listFragment.toBookList()
    }

    override suspend fun updateListBookPositions(
        listId: Int,
        startPosition: Int,
        orderedListBookIds: List<Int>,
    ) {
        if (orderedListBookIds.isEmpty()) return

        withContext(appDispatchers.io) {
            val clearedPositions = (startPosition until startPosition + orderedListBookIds.size).toList()

            val updates = orderedListBookIds.mapIndexed { index, listBookId ->
                List_books_updates(
                    _set = Optional.Present(
                        List_books_set_input(
                            position = Optional.Present(startPosition + index),
                        ),
                    ),
                    where = List_books_bool_exp(
                        id = Optional.Present(
                            Int_comparison_exp(_eq = Optional.Present(listBookId)),
                        ),
                    ),
                )
            }

            apolloClient.safeMutation(
                mutation = UpdateListBookPositionsMutation(
                    listId = listId,
                    clearedPositions = clearedPositions,
                    updates = updates,
                ),
            )
        }
    }

    override suspend fun setListRanked(
        listId: Int,
        ranked: Boolean,
    ): BookList = withContext(appDispatchers.io) {
        val input = ListInput(
            ranked = Optional.Present(ranked),
        )

        val response = apolloClient
            .safeMutation(mutation = UpdateListMutation(
                id = listId,
                list = input,
            ),)
            .listResponse
            ?: throw Exception("Did not receive an update-list response")

        val errorText: String? = response.errors?.takeIf { it.isNotBlank() }

        if (errorText != null) {
            throw Exception("UpdateList rejected by server: $errorText")
        }

        val listFragment = response
            .list
            ?.updateListListFragment()
            ?: throw Exception("Did not receive a list fragment")

        listFragment.toBookList()
    }
}

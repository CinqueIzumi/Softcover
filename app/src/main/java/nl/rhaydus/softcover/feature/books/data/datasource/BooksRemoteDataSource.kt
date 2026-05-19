package nl.rhaydus.softcover.feature.books.data.datasource

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rhaydus.softcover.GetBookByIdQuery
import nl.rhaydus.softcover.GetBookByIdQuery.Data.Book.Companion.bookDetailFragment
import nl.rhaydus.softcover.GetBookIdByEditionIdQuery
import nl.rhaydus.softcover.GetBooksByIdsQuery
import nl.rhaydus.softcover.GetBooksByIdsQuery.Data.Book.Companion.bookDetailFragment as booksByIdsBookDetailFragment
import nl.rhaydus.softcover.GetEditionsByBookIdQuery
import nl.rhaydus.softcover.GetEditionsByBookIdQuery.Data.Edition.Companion.editionDetailFragment
import nl.rhaydus.softcover.GetEditionsByIdsQuery
import nl.rhaydus.softcover.GetEditionsByIdsQuery.Data.Edition.Companion.editionDetailFragment as editionsByIdsDetailFragment
import nl.rhaydus.softcover.GetUserBookListsQuery
import nl.rhaydus.softcover.GetUserBookListsQuery.Data.Me.List.Companion.listFragment
import nl.rhaydus.softcover.GetUserBooksQuery
import nl.rhaydus.softcover.GetUserBooksQuery.Data.Me.User_book.Companion.userBookFragment
import nl.rhaydus.softcover.MarkBookAsReadMutation
import nl.rhaydus.softcover.MarkBookAsReadMutation.Data.Insert_user_book.User_book.Companion.userBookFragment
import nl.rhaydus.softcover.MarkBookAsReadingMutation
import nl.rhaydus.softcover.MarkBookAsReadingMutation.Data.Update_user_book.User_book.Companion.userBookFragment
import nl.rhaydus.softcover.MarkBookAsWantToReadMutation
import nl.rhaydus.softcover.MarkBookAsWantToReadMutation.Data.Insert_user_book.User_book.Companion.userBookFragment
import nl.rhaydus.softcover.MarkEditionAsOwnedMutation
import nl.rhaydus.softcover.MarkEditionAsOwnedMutation.Data.Edition_owned.List_book.Companion.listBookFragment
import nl.rhaydus.softcover.RemoveListBookMutation
import nl.rhaydus.softcover.RemoveListBookMutation.Data.Delete_list_book.List.Companion.listFragment
import nl.rhaydus.softcover.RemoveUserBookMutation
import nl.rhaydus.softcover.UpdateBookEditionMutation
import nl.rhaydus.softcover.UpdateBookEditionMutation.Data.Update_user_book.User_book.Companion.userBookFragment
import nl.rhaydus.softcover.UpdateReadingProgressMutation
import nl.rhaydus.softcover.UpdateReadingProgressMutation.Data.Update_user_book_read.User_book_read.User_book.Companion.userBookFragment
import com.apollographql.apollo.cache.normalized.FetchPolicy
import nl.rhaydus.softcover.core.data.network.helper.safeMutation
import nl.rhaydus.softcover.core.data.network.helper.safeQuery
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.books.data.mapper.toBook
import nl.rhaydus.softcover.feature.books.data.mapper.toBookEdition
import nl.rhaydus.softcover.feature.books.data.mapper.toBookList
import nl.rhaydus.softcover.feature.books.data.mapper.toListBook
import nl.rhaydus.softcover.type.DatesReadInput
import nl.rhaydus.softcover.type.Int_comparison_exp
import nl.rhaydus.softcover.type.Lists_bool_exp
import nl.rhaydus.softcover.type.UserBookCreateInput
import nl.rhaydus.softcover.type.UserBookUpdateInput
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter

interface BooksRemoteDataSource {
    suspend fun fetchBookById(id: Int): Book

    suspend fun fetchBookIdForEdition(editionId: Int): Int?

    suspend fun fetchBooksByIds(
        ids: List<Int>,
        forceNetwork: Boolean = false,
    ): List<Book>

    suspend fun markBookAsWantToRead(bookId: Int): Book

    suspend fun markBookAsReading(book: Book): Book

    suspend fun removeBookFromLibrary(book: Book)

    suspend fun initializeBooks(
        userId: Int,
        statusIds: Set<Int>? = null,
    ): List<Book>

    suspend fun fetchUserLists(
        userId: Int,
        listIds: Set<Int>? = null,
    ): List<BookList>

    suspend fun getEditionsByBookId(bookId: Int): List<BookEdition>

    suspend fun fetchEditionsByIds(
        ids: List<Int>,
        forceNetwork: Boolean = false,
    ): List<BookEdition>

    suspend fun updateBookProgress(
        book: Book,
        newPage: Int? = null,
        newSeconds: Int? = null,
    ): Book

    suspend fun markBookAsRead(book: Book): Book

    suspend fun updateBookEdition(
        userBook: UserBook,
        newEditionId: Int,
    ): Book

    suspend fun markEditionAsOwned(edition: BookEdition): ListBook

    suspend fun removeListBook(book: ListBook): BookList

    suspend fun replayUpdateBookProgress(
        userBookReadId: Int,
        editionId: Int?,
        progressPages: Int?,
        progressSeconds: Int?,
        startedAt: String?,
        finishedAt: String?,
    )

    suspend fun replayMarkBookAsRead(
        bookId: Int,
        userDate: String,
    )
}

private const val BATCH_ID_LIMIT: Int = 200

class BooksRemoteDataSourceImpl(
    private val apolloClient: ApolloClient,
) : BooksRemoteDataSource {
    override suspend fun fetchBookById(id: Int): Book {
        val book = fetchBookByIdRaw(id)

        val canonicalId = book.canonicalId

        if (canonicalId != null && canonicalId != book.id) {
            Timber.i("-=- Book $id has canonical $canonicalId; refetching canonical.")
            return fetchBookByIdRaw(canonicalId).copy(canonicalId = null)
        }

        return book
    }

    private suspend fun fetchBookByIdRaw(id: Int): Book {
        val result = apolloClient.safeQuery(
            query = GetBookByIdQuery(id = id),
            fetchPolicy = FetchPolicy.CacheFirst,
        )

        val raw = result.books.firstOrNull() ?: throw BookNotFoundException(bookId = id)

        return raw.bookDetailFragment()?.toBook()
            ?: throw Exception("Book $id could not be mapped")
    }

    override suspend fun fetchBookIdForEdition(editionId: Int): Int? {
        val result = apolloClient.safeQuery(
            query = GetBookIdByEditionIdQuery(editionId = editionId),
            fetchPolicy = FetchPolicy.NetworkFirst,
        )

        return result.editions.firstOrNull()?.book_id
    }

    override suspend fun fetchBooksByIds(
        ids: List<Int>,
        forceNetwork: Boolean,
    ): List<Book> {
        if (ids.isEmpty()) return emptyList()

        val policy = if (forceNetwork) FetchPolicy.NetworkFirst else FetchPolicy.CacheFirst

        return ids.chunked(BATCH_ID_LIMIT).flatMap { chunk ->
            val result = apolloClient.safeQuery(
                query = GetBooksByIdsQuery(ids = chunk),
                fetchPolicy = policy,
            )

            result.books.mapNotNull { it.booksByIdsBookDetailFragment()?.toBook() }
        }
    }

    override suspend fun getEditionsByBookId(bookId: Int): List<BookEdition> {
        val result = apolloClient.safeQuery(
            query = GetEditionsByBookIdQuery(bookId = bookId),
            fetchPolicy = FetchPolicy.CacheFirst,
        )

        return result.editions.mapNotNull { it.editionDetailFragment()?.toBookEdition() }
    }

    override suspend fun fetchEditionsByIds(
        ids: List<Int>,
        forceNetwork: Boolean,
    ): List<BookEdition> {
        if (ids.isEmpty()) return emptyList()

        val policy = if (forceNetwork) FetchPolicy.NetworkFirst else FetchPolicy.CacheFirst

        return ids.chunked(BATCH_ID_LIMIT).flatMap { chunk ->
            val result = apolloClient.safeQuery(
                query = GetEditionsByIdsQuery(ids = chunk),
                fetchPolicy = policy,
            )

            result.editions.mapNotNull { it.editionsByIdsDetailFragment()?.toBookEdition() }
        }
    }

    override suspend fun markBookAsWantToRead(bookId: Int): Book {
        val userBookCreateInput = UserBookCreateInput(
            book_id = bookId,
            status_id = Optional.Present(UserBookStatus.WANT_TO_READ.code),
            privacy_setting_id = Optional.Present(PrivacySetting.PUBLIC.code),
        )

        return apolloClient
            .safeMutation(mutation = MarkBookAsWantToReadMutation(`object` = userBookCreateInput))
            .insert_user_book
            ?.user_book
            ?.userBookFragment()
            ?.toBook() ?: throw Exception("Book could not be mapped")
    }

    override suspend fun markBookAsReading(book: Book): Book {
        val userBook = book.userBook
            ?: throw Exception("User did not have a user book")

        val currentDate = LocalDate
            .now()
            .format(DateTimeFormatter.ISO_LOCAL_DATE)

        val input = UserBookUpdateInput(
            edition_id = Optional.Present(book.currentEdition?.id),
            review_has_spoilers = Optional.Present(userBook.reviewHasSpoilers),
            status_id = Optional.present(UserBookStatus.CURRENTLY_READING.code),
            last_read_date = Optional.Present(userBook.lastReadDate),
            rating = Optional.Present(userBook.rating),
            privacy_setting_id = Optional.Present(PrivacySetting.PUBLIC.code),
            referrer_user_id = Optional.Present(userBook.referrerUserId),
            reviewed_at = Optional.Present(userBook.reviewedAt),
            date_added = Optional.Present(userBook.dateAdded),
            user_date = Optional.present(currentDate)
        )

        return apolloClient
            .safeMutation(
                mutation = MarkBookAsReadingMutation(
                    id = userBook.id,
                    `object` = input
                )
            )
            .update_user_book
            ?.user_book
            ?.userBookFragment()
            ?.toBook() ?: throw Exception("Book could not be mapped")
    }

    override suspend fun removeBookFromLibrary(book: Book) {
        val userBookId = book.userBook?.id
            ?: throw Exception("User did not have a user book")

        apolloClient.safeMutation(mutation = RemoveUserBookMutation(id = userBookId))
    }

    override suspend fun initializeBooks(
        userId: Int,
        statusIds: Set<Int>?,
    ): List<Book> = withContext(Dispatchers.IO) {
        val result = apolloClient.safeQuery(
            query = GetUserBooksQuery(statusIds = Optional.presentIfNotNull(statusIds?.toList())),
        )

        val userBooks = result.me.firstOrNull()?.user_books
            ?: throw Exception("No books were found")

        val mapped = userBooks.mapNotNull { it.userBookFragment()?.toBook() }

        resolveCanonicalMerges(books = mapped)
    }

    private suspend fun resolveCanonicalMerges(books: List<Book>): List<Book> {
        val canonicalIds = books.mapNotNull { it.canonicalId }.distinct()

        if (canonicalIds.isEmpty()) return books

        Timber.i("-=- Resolving ${canonicalIds.size} canonical merge(s): $canonicalIds")

        val canonicalBooks = fetchBooksByIds(ids = canonicalIds).associateBy { it.id }

        return books.map { book ->
            val canonicalId = book.canonicalId ?: return@map book

            val canonical = canonicalBooks[canonicalId]

            if (canonical == null) {
                Timber.w("-=- Canonical $canonicalId for book ${book.id} not returned; keeping pre-merge metadata.")
                return@map book
            }
            book.withCanonicalMetadata(canonical = canonical)
        }
    }

    private fun Book.withCanonicalMetadata(canonical: Book): Book = copy(
        id = canonical.id,
        canonicalId = null,
        title = canonical.title,
        rating = canonical.rating,
        description = canonical.description,
        releaseYear = canonical.releaseYear,
        releaseDate = canonical.releaseDate,
        coverUrl = canonical.coverUrl,
        authors = canonical.authors,
        usersCount = canonical.usersCount,
        ratingsCount = canonical.ratingsCount,
        bookSeries = canonical.bookSeries,
        positionsInSeries = canonical.positionsInSeries,
        isCompilation = canonical.isCompilation,
    )

    override suspend fun fetchUserLists(
        userId: Int,
        listIds: Set<Int>?,
    ): List<BookList> = withContext(Dispatchers.IO) {
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
            list.listFragment()?.toBookList()
        }
    }

    override suspend fun updateBookProgress(
        book: Book,
        newPage: Int?,
        newSeconds: Int?,
    ): Book {
        val userBook = book.userBook
            ?: throw Exception("Book did not contain a user book")

        val userBookRead =
            book.userBookRead ?: throw Exception("Book did not contain a user book read")

        val isAudiobook = newSeconds != null

        val dataObject = DatesReadInput(
            progress_pages = if (isAudiobook) Optional.absent() else Optional.present(newPage),
            progress_seconds = if (isAudiobook) Optional.present(newSeconds) else Optional.absent(),
            started_at = Optional.present(userBookRead.startedAt),
            finished_at = Optional.present(userBookRead.finishedAt),
            edition_id = Optional.present(userBook.editionId),
        )

        val mutation = UpdateReadingProgressMutation(
            id = userBookRead.id,
            datesReadInput = dataObject
        )

        val userBookFragment = apolloClient
            .safeMutation(mutation = mutation)
            .update_user_book_read
            ?.user_book_read
            ?.user_book
            ?.userBookFragment()
            ?: throw Exception("Did not receive a new user book fragment")

        return userBookFragment.toBook() ?: throw Exception("Book could not be mapped")
    }

    override suspend fun markBookAsRead(book: Book): Book {
        val currentDate = LocalDate.now().toString()

        val dataObject = UserBookCreateInput(
            book_id = book.id,
            status_id = Optional.present(UserBookStatus.READ.code),
            user_date = Optional.present(currentDate),
            privacy_setting_id = Optional.present(PrivacySetting.PUBLIC.code),
        )

        val mutation = MarkBookAsReadMutation(userBookCreateInput = dataObject)

        val userBookFragment = apolloClient
            .safeMutation(mutation = mutation)
            .insert_user_book
            ?.user_book
            ?.userBookFragment()
            ?: throw Exception("Did not receive a new user book fragment")

        return userBookFragment.toBook() ?: throw Exception("Book was not mapped successfully")
    }

    override suspend fun updateBookEdition(
        userBook: UserBook,
        newEditionId: Int,
    ): Book {
        val mutation = UpdateBookEditionMutation(
            id = userBook.id,
            `object` = UserBookUpdateInput(
                date_added = Optional.Present(userBook.dateAdded),
                edition_id = Optional.present(newEditionId),
                last_read_date = Optional.Present(userBook.lastReadDate),
                privacy_setting_id = Optional.Present(PrivacySetting.PUBLIC.code),
                rating = Optional.Present(userBook.rating),
                referrer_user_id = Optional.Present(userBook.referrerUserId),
                review_has_spoilers = Optional.Present(userBook.reviewHasSpoilers),
                status_id = Optional.present(userBook.status.code),
                reviewed_at = Optional.Present(userBook.reviewedAt)
            )
        )

        val userBookFragment = apolloClient
            .safeMutation(mutation = mutation)
            .update_user_book
            ?.user_book
            ?.userBookFragment()
            ?: throw Exception("Did not receive a new user book fragment")

        return userBookFragment.toBook() ?: throw Exception("Book was not mapped successfully")
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

    override suspend fun removeListBook(book: ListBook): BookList {
        val mutation = RemoveListBookMutation(id = book.listBookId)

        val listFragment = apolloClient
            .safeMutation(mutation = mutation)
            .delete_list_book
            ?.list
            ?.listFragment()
            ?: throw Exception("Did not receive a list fragment")

        return listFragment.toBookList()
    }

    override suspend fun replayUpdateBookProgress(
        userBookReadId: Int,
        editionId: Int?,
        progressPages: Int?,
        progressSeconds: Int?,
        startedAt: String?,
        finishedAt: String?,
    ) {
        val isAudiobook = progressSeconds != null

        val dataObject = DatesReadInput(
            progress_pages = if (isAudiobook) Optional.absent() else Optional.present(progressPages),
            progress_seconds = if (isAudiobook) Optional.present(progressSeconds) else Optional.absent(),
            started_at = Optional.present(startedAt),
            finished_at = Optional.present(finishedAt),
            edition_id = Optional.present(editionId),
        )

        apolloClient.safeMutation(
            mutation = UpdateReadingProgressMutation(
                id = userBookReadId,
                datesReadInput = dataObject,
            ),
        )
    }

    override suspend fun replayMarkBookAsRead(
        bookId: Int,
        userDate: String,
    ) {
        val dataObject = UserBookCreateInput(
            book_id = bookId,
            status_id = Optional.present(UserBookStatus.READ.code),
            user_date = Optional.present(userDate),
            privacy_setting_id = Optional.present(PrivacySetting.PUBLIC.code),
        )

        apolloClient.safeMutation(mutation = MarkBookAsReadMutation(userBookCreateInput = dataObject))
    }
}

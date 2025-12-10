package nl.rhaydus.softcover.feature.reading.data.datasource

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.apollographql.apollo.cache.normalized.watch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import nl.rhaydus.softcover.GetCurrentlyReadingBooksQuery
import nl.rhaydus.softcover.MarkBookAsReadMutation
import nl.rhaydus.softcover.UpdateBookEditionMutation
import nl.rhaydus.softcover.UpdateReadingProgressMutation
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.reading.domain.model.BookWithProgress
import nl.rhaydus.softcover.fragment.EditionFragment
import nl.rhaydus.softcover.fragment.UserBookFragment
import nl.rhaydus.softcover.fragment.UserBookReadFragment
import nl.rhaydus.softcover.type.DatesReadInput
import nl.rhaydus.softcover.type.UserBookCreateInput
import nl.rhaydus.softcover.type.UserBookUpdateInput
import java.time.LocalDate
import javax.inject.Inject

// TODO: Maybe move these to mappers or something of the sort
// TODO: Maybe look into the correct default values to prevent weird app ui?
fun EditionFragment.toBookEdition(): BookEdition {
    return BookEdition(
        id = id,
        title = title,
        url = image?.url,
        publisher = publisher?.name,
        pages = pages,
        authors = contributions.map { contribution ->
            Author(name = contribution.author?.name ?: "")
        },
        isbn10 = isbn_10,
    )
}

fun UserBookFragment.toBook(): Book {
    val book = book

    return Book(
        id = book.id,
        title = book.title ?: "",
        editions = book.editions.map { userBookEdition ->
            userBookEdition.editionFragment.toBookEdition()
        }
    )
}

fun UserBookReadFragment.toBookWithProgress(): BookWithProgress? {
    val userBookFragment = user_book?.userBookFragment ?: return null
    val editionId = edition?.editionFragment?.id ?: return null
    val book = userBookFragment.toBook()

    return BookWithProgress(
        book = book,
        currentPage = progress_pages ?: 0,
        progress = progress?.toFloat() ?: 0f,
        editionId = editionId,
        userBookReadId = id,
        startedAt = started_at,
        finishedAt = finished_at,
        userBookId = userBookFragment.id
    )
}

class BookRemoteDataSourceImpl @Inject constructor(
    private val apolloClient: ApolloClient,
) : BookRemoteDataSource {
    override fun getCurrentlyReadingBooks(userId: Int): Flow<List<BookWithProgress>> {
        return apolloClient
            .query(query = GetCurrentlyReadingBooksQuery(userId = userId))
            .fetchPolicy(FetchPolicy.CacheAndNetwork)
            .watch()
            .mapNotNull { result ->
                println("-=- watcher was called")
                val userBooks = result.data?.user_books ?: return@mapNotNull null

                userBooks.mapNotNull { userBook ->
                    val userBookRead =
                        userBook.user_book_reads.firstOrNull() ?: return@mapNotNull null

                    userBookRead.userBookReadFragment.toBookWithProgress()
                }
            }
    }

    override suspend fun refreshCurrentlyReadingBooks(userId: Int) {
        apolloClient
            .query(query = GetCurrentlyReadingBooksQuery(userId = userId))
            .fetchPolicy(FetchPolicy.NetworkOnly)
            .execute()
    }

    override suspend fun updateBookProgress(
        book: BookWithProgress,
        newPage: Int,
    ) {
        val dataObject = DatesReadInput(
            progress_pages = Optional.present(newPage),
            started_at = Optional.present(book.startedAt),
            finished_at = Optional.present(book.finishedAt),
            edition_id = Optional.present(book.editionId),
        )

        val mutation = UpdateReadingProgressMutation(
            id = book.userBookReadId,
            datesReadInput = dataObject
        )

        val result = apolloClient
            .mutation(mutation = mutation)
            .execute()

        result.dataOrThrow()
    }

    override suspend fun markBookAsRead(book: BookWithProgress) {
        val currentDate = LocalDate.now().toString()

        val dataObject = UserBookCreateInput(
            book_id = book.book.id,
            status_id = Optional.present(UserBookStatus.READ.code),
            user_date = Optional.present(currentDate)
        )

        val mutation = MarkBookAsReadMutation(userBookCreateInput = dataObject)

        val result = apolloClient
            .mutation(mutation = mutation)
            .execute()

        val data = result.dataOrThrow()

        if (data.insert_user_book?.error != null) {
            throw Exception("Error while marking book as read")
        }
    }

    override suspend fun updateBookEdition(
        userBookId: Int,
        newEditionId: Int,
        userId: Int,
    ) {
        val mutation = UpdateBookEditionMutation(
            id = userBookId,
            `object` = UserBookUpdateInput(
                edition_id = Optional.present(newEditionId)
            )
        )

        val result = apolloClient
            .mutation(mutation = mutation)
            .execute()

        result.dataOrThrow()

        refreshCurrentlyReadingBooks(userId = userId)
    }
}
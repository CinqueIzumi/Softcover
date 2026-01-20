package nl.rhaydus.softcover.feature.reading.data.datasource

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.apollographql.apollo.cache.normalized.watch
import com.apollographql.apollo.exception.ApolloHttpException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import nl.rhaydus.softcover.GetCurrentlyReadingBooksQuery
import nl.rhaydus.softcover.MarkBookAsReadMutation
import nl.rhaydus.softcover.UpdateBookEditionMutation
import nl.rhaydus.softcover.UpdateReadingProgressMutation
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.reading.data.mapper.toBookWithProgress
import nl.rhaydus.softcover.feature.reading.domain.model.BookWithProgress
import nl.rhaydus.softcover.type.DatesReadInput
import nl.rhaydus.softcover.type.UserBookCreateInput
import nl.rhaydus.softcover.type.UserBookUpdateInput
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

class BookRemoteDataSourceImpl @Inject constructor(
    private val apolloClient: ApolloClient,
) : BookRemoteDataSource {
    override fun getCurrentlyReadingBooks(userId: Int): Flow<List<BookWithProgress>> {
        return apolloClient
            .query(query = GetCurrentlyReadingBooksQuery(userId = userId))
            .fetchPolicy(FetchPolicy.CacheAndNetwork)
            .watch()
            .mapNotNull { result ->
                if (userId == -1) return@mapNotNull emptyList()

                result.exception?.let { exception ->
                    Timber.e("-=- Something went wrong fetching currently reading! ${exception.message}")

                    if (exception is ApolloHttpException && exception.statusCode == 401) {
                        return@mapNotNull emptyList()
                    }
                }

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
            .dataOrThrow()
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

        apolloClient
            .mutation(mutation = mutation)
            .execute()
            .dataOrThrow()
    }

    override suspend fun markBookAsRead(book: BookWithProgress) {
        val currentDate = LocalDate.now().toString()

        val dataObject = UserBookCreateInput(
            book_id = book.book.id,
            status_id = Optional.present(UserBookStatus.READ.code),
            user_date = Optional.present(currentDate)
        )

        val mutation = MarkBookAsReadMutation(userBookCreateInput = dataObject)

        apolloClient
            .mutation(mutation = mutation)
            .execute()
            .dataOrThrow()
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

        apolloClient
            .mutation(mutation = mutation)
            .execute()
            .dataOrThrow()
    }
}
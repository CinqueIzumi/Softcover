package nl.rhaydus.softcover.feature.reading.data.datasource

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import nl.rhaydus.softcover.MarkBookAsReadMutation
import nl.rhaydus.softcover.UpdateBookEditionMutation
import nl.rhaydus.softcover.UpdateReadingProgressMutation
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.book.data.mapper.toBook
import nl.rhaydus.softcover.type.DatesReadInput
import nl.rhaydus.softcover.type.UserBookCreateInput
import nl.rhaydus.softcover.type.UserBookUpdateInput
import java.time.LocalDate

class BookRemoteDataSourceImpl(
    private val apolloClient: ApolloClient,
) : BookRemoteDataSource {
    override suspend fun updateBookProgress(
        book: Book,
        newPage: Int,
    ): Book {
        val userBookReadId = book.userBookReadId
            ?: throw Exception("Book did not have a user book read id")

        val dataObject = DatesReadInput(
            progress_pages = Optional.present(newPage),
            started_at = Optional.present(book.startedAt),
            finished_at = Optional.present(book.finishedAt),
            edition_id = Optional.present(book.userEditionId),
        )

        val mutation = UpdateReadingProgressMutation(
            id = userBookReadId,
            datesReadInput = dataObject
        )

        val result = apolloClient
            .mutation(mutation = mutation)
            .execute()
            .dataOrThrow()

        val userBookReadFragment =
            result.update_user_book_read?.user_book_read?.userBookReadFragment
                ?: throw Exception("Did not receive a new user book read fragment")

        val updatedBook = book.copy(
            currentPage = userBookReadFragment.progress_pages,
            progress = userBookReadFragment.progress?.toFloat(),
        )

        return updatedBook
    }

    override suspend fun markBookAsRead(book: Book): Book {
        val currentDate = LocalDate.now().toString()

        val dataObject = UserBookCreateInput(
            book_id = book.id,
            status_id = Optional.present(UserBookStatus.READ.code),
            user_date = Optional.present(currentDate)
        )

        val mutation = MarkBookAsReadMutation(userBookCreateInput = dataObject)

        val result = apolloClient
            .mutation(mutation = mutation)
            .execute()
            .dataOrThrow()

        val userBookFragment = result.insert_user_book?.user_book?.userBookFragment
            ?: throw Exception("Did not receive a new user book fragment")

        return userBookFragment.toBook()
    }

    override suspend fun updateBookEdition(
        userBookId: Int,
        newEditionId: Int,
    ): Book {
        val mutation = UpdateBookEditionMutation(
            id = userBookId,
            `object` = UserBookUpdateInput(
                edition_id = Optional.present(newEditionId)
            )
        )

        val result = apolloClient
            .mutation(mutation = mutation)
            .execute()
            .dataOrThrow()

        val userBookFragment = result.update_user_book?.user_book?.userBookFragment
            ?: throw Exception("Did not receive a new user book fragment")

        return userBookFragment.toBook()
    }
}
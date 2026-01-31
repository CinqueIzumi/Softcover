package nl.rhaydus.softcover.feature.reading.data.datasource

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import nl.rhaydus.softcover.MarkBookAsReadMutation
import nl.rhaydus.softcover.UpdateBookEditionMutation
import nl.rhaydus.softcover.UpdateReadingProgressMutation
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
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
    ) {
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

        apolloClient
            .mutation(mutation = mutation)
            .execute()
            .dataOrThrow()
    }

    override suspend fun markBookAsRead(book: Book) {
        val currentDate = LocalDate.now().toString()

        val dataObject = UserBookCreateInput(
            book_id = book.id,
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
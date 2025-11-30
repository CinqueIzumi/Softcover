package nl.rhaydus.softcover.feature.reading.data.datasource

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import nl.rhaydus.softcover.GetCurrentlyReadingBooksQuery
import nl.rhaydus.softcover.MarkBookAsReadMutation
import nl.rhaydus.softcover.UpdateReadingProgressMutation
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.reading.domain.model.BookWithProgress
import nl.rhaydus.softcover.type.DatesReadInput
import nl.rhaydus.softcover.type.UserBookCreateInput
import java.time.LocalDate
import javax.inject.Inject

class BookRemoteDataSourceImpl @Inject constructor(
    private val apolloClient: ApolloClient,
) : BookRemoteDataSource {
    override suspend fun getCurrentlyReadingBooks(userId: Int): List<BookWithProgress> {
        val result = apolloClient
            .query(query = GetCurrentlyReadingBooksQuery(userId = userId))
            .execute()

        val books = result.data?.user_books ?: emptyList()

        val mappedBooks = books.map {
            val authors = it.book.contributions.map { contribution ->
                Author(name = contribution.author?.name ?: "")
            }

            val userBooksReads = it.user_book_reads.firstOrNull()

            val book = Book(
                id = it.book.id,
                title = it.book.title ?: "",
                url = userBooksReads?.edition?.image?.url ?: "",
                authors = authors,
                totalPages = it.book.pages ?: -1
            )

            BookWithProgress(
                book = book,
                currentPage = userBooksReads?.progress_pages ?: 0,
                progress = userBooksReads?.progress?.toFloat() ?: 0f,
                editionId = userBooksReads?.edition?.id ?: -1,
                userProgressId = userBooksReads?.id ?: -1,
                startedAt = userBooksReads?.started_at,
                finishedAt = userBooksReads?.finished_at,
            )
        }

        return mappedBooks
    }

    // TODO: Add support for updating with progress %
    override suspend fun updateBookProgress(
        book: BookWithProgress,
        newPage: Int,
    ): BookWithProgress {
        val dataObject = DatesReadInput(
            progress_pages = Optional.present(newPage),
            started_at = Optional.present(book.startedAt),
            finished_at = Optional.present(book.finishedAt),
            edition_id = Optional.present(book.editionId),
        )

        val mutation = UpdateReadingProgressMutation(
            id = book.userProgressId,
            datesReadInput = dataObject
        )

        val result = apolloClient
            .mutation(mutation = mutation)
            .execute()

        val data = result.dataOrThrow().update_user_book_read?.user_book_read
            ?: throw Exception("No book was returned")

        val updatedBook = book.copy(
            progress = data.progress?.toFloat() ?: throw Exception("No progress was returned"),
            currentPage = data.progress_pages ?: throw Exception("No current page was returned"),
        )

        return updatedBook
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
}
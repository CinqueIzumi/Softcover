package nl.rhaydus.softcover.feature.reading.data.datasource

import com.apollographql.apollo.ApolloClient
import nl.rhaydus.softcover.GetCurrentlyReadingBooksQuery
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.reading.domain.model.BookWithProgress
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
                progress = userBooksReads?.progress?.toFloat() ?: 0f
            )
        }

        return mappedBooks
    }
}
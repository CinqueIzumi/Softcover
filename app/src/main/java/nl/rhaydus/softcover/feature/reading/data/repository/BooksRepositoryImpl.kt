package nl.rhaydus.softcover.feature.reading.data.repository

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.reading.data.datasource.BookRemoteDataSource
import nl.rhaydus.softcover.feature.reading.domain.repository.BooksRepository

class BooksRepositoryImpl(
    private val bookRemoteDataSource: BookRemoteDataSource,
) : BooksRepository {
    override suspend fun updateBookProgress(
        book: Book,
        newPage: Int,
    ): Book {
        return bookRemoteDataSource.updateBookProgress(
            book = book,
            newPage = newPage,
        )
    }

    override suspend fun markBookAsRead(book: Book): Book {
        return bookRemoteDataSource.markBookAsRead(book = book)
    }

    override suspend fun updateBookEdition(
        userBookId: Int,
        newEditionId: Int,
    ): Book {
        return bookRemoteDataSource.updateBookEdition(
            userBookId = userBookId,
            newEditionId = newEditionId,
        )
    }
}

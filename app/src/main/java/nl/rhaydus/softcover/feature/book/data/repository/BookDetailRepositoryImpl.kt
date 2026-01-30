package nl.rhaydus.softcover.feature.book.data.repository

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.enum.BookStatus
import nl.rhaydus.softcover.feature.book.data.datasource.BookDetailRemoteDataSource
import nl.rhaydus.softcover.feature.book.domain.repository.BookDetailRepository

class BookDetailRepositoryImpl(
    private val bookDetailRemoteDataSource: BookDetailRemoteDataSource,
) : BookDetailRepository {
    override suspend fun fetchBookById(
        id: Int,
    ): Book {
        return bookDetailRemoteDataSource.fetchBookById(id = id)
    }

    override suspend fun updateBookStatus(
        book: Book,
        newStatus: BookStatus,
    ) {
        bookDetailRemoteDataSource.updateBookStatus(
            book = book,
            newStatus = newStatus,
        )
    }
}
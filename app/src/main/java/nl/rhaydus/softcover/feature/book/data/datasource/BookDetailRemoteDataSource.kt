package nl.rhaydus.softcover.feature.book.data.datasource

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.enum.BookStatus

interface BookDetailRemoteDataSource {
    suspend fun fetchBookById(
        id: Int,
        userId: Int,
    ): Book

    suspend fun updateBookStatus(
        book: Book,
        newStatus: BookStatus,
        userId: Int,
    )
}
package nl.rhaydus.softcover.feature.book.domain.repository

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.enum.BookStatus

interface BookDetailRepository {
    suspend fun fetchBookById(
        id: Int,
    ): Book

    suspend fun updateBookStatus(
        book: Book,
        newStatus: BookStatus,
    )
}
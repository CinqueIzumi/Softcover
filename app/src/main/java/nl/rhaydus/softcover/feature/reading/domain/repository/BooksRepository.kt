package nl.rhaydus.softcover.feature.reading.domain.repository

import nl.rhaydus.softcover.core.domain.model.Book

interface BooksRepository {
    suspend fun updateBookProgress(
        book: Book,
        newPage: Int,
    )

    suspend fun markBookAsRead(book: Book)

    suspend fun updateBookEdition(
        userBookId: Int,
        newEditionId: Int,
    )
}
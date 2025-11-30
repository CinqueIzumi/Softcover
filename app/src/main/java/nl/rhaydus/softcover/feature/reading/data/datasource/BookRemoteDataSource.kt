package nl.rhaydus.softcover.feature.reading.data.datasource

import nl.rhaydus.softcover.feature.reading.domain.model.BookWithProgress

interface BookRemoteDataSource {
    suspend fun getCurrentlyReadingBooks(userId: Int): List<BookWithProgress>

    suspend fun updateBookProgress(
        book: BookWithProgress,
        newPage: Int,
    ): BookWithProgress

    suspend fun markBookAsRead(book: BookWithProgress)
}
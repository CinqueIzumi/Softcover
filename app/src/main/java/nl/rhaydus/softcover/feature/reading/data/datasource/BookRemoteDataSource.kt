package nl.rhaydus.softcover.feature.reading.data.datasource

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.reading.domain.model.BookWithProgress

interface BookRemoteDataSource {
    fun getCurrentlyReadingBooks(userId: Int): Flow<List<BookWithProgress>>

    suspend fun refreshCurrentlyReadingBooks(userId: Int)

    suspend fun updateBookProgress(
        book: BookWithProgress,
        newPage: Int,
    )

    suspend fun markBookAsRead(book: BookWithProgress)

    suspend fun updateBookEdition(
        userBookId: Int,
        newEditionId: Int,
        userId: Int,
    )
}
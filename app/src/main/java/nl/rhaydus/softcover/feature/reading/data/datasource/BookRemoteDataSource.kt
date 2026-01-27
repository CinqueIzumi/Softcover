package nl.rhaydus.softcover.feature.reading.data.datasource

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.Book

interface BookRemoteDataSource {
    fun getCurrentlyReadingBooks(userId: Int): Flow<List<Book>>

    suspend fun refreshCurrentlyReadingBooks(userId: Int)

    suspend fun updateBookProgress(
        book: Book,
        newPage: Int,
    )

    suspend fun markBookAsRead(book: Book)

    suspend fun updateBookEdition(
        userBookId: Int,
        newEditionId: Int,
        userId: Int,
    )
}
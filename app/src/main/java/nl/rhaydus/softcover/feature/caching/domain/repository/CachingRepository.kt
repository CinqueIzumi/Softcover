package nl.rhaydus.softcover.feature.caching.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.UserBookStatus

interface CachingRepository {
    val books: Flow<List<Book>>

    fun getBooksFlowByStatus(status: UserBookStatus): Flow<List<Book>>

    suspend fun initializeBooks(userId: Int)

    suspend fun cacheBook(book: Book)

    suspend fun cacheBooks(books: List<Book>)
}
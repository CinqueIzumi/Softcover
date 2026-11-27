package nl.rhaydus.softcover.core.corpus.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.BookMood

interface BookMoodsRepository {
    fun observeAll(): Flow<Map<Int, List<BookMood>>>

    fun observe(bookId: Int): Flow<List<BookMood>>

    suspend fun save(
        bookId: Int,
        moods: List<BookMood>,
    )
}

package nl.rhaydus.softcover.feature.personal.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.personal.domain.model.Highlight

interface HighlightRepository {
    fun observeByBookId(bookId: Int): Flow<List<Highlight>>

    fun observeAll(): Flow<List<Highlight>>

    fun observeById(id: Long): Flow<Highlight?>

    suspend fun add(
        bookId: Int,
        quote: String,
        page: Int?,
        note: String?,
    ): Long

    suspend fun update(highlight: Highlight)

    suspend fun delete(id: Long)
}

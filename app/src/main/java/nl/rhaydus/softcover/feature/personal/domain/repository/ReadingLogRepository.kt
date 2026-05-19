package nl.rhaydus.softcover.feature.personal.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.personal.domain.model.ReadingLogEntry
import java.time.LocalDate

interface ReadingLogRepository {
    fun observeByBookId(bookId: Int): Flow<List<ReadingLogEntry>>

    fun observeCountByBookId(bookId: Int): Flow<Int>

    suspend fun add(
        bookId: Int,
        startedAt: LocalDate?,
        finishedAt: LocalDate?,
        rating: Double?,
        note: String?,
    ): Long

    suspend fun update(entry: ReadingLogEntry)

    suspend fun delete(id: Long)
}

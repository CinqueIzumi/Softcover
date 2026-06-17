package nl.rhaydus.softcover.core.personal.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.personal.domain.model.ReadingLogEntry

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

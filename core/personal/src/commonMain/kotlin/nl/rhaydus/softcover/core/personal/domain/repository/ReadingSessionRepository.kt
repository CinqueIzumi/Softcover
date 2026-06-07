package nl.rhaydus.softcover.core.personal.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.ReadingSession

interface ReadingSessionRepository {
    fun observeByBookId(bookId: Int): Flow<List<ReadingSession>>

    fun observeAll(): Flow<List<ReadingSession>>

    fun observeActive(): Flow<ReadingSession?>

    fun observeById(id: Long): Flow<ReadingSession?>

    suspend fun start(
        bookId: Int,
        startPage: Int?,
        startSeconds: Int?,
    ): Long

    suspend fun stop(
        id: Long,
        endPage: Int?,
        endSeconds: Int?,
    )

    suspend fun pause(id: Long)

    suspend fun resume(id: Long)

    suspend fun update(session: ReadingSession)

    suspend fun delete(id: Long)
}

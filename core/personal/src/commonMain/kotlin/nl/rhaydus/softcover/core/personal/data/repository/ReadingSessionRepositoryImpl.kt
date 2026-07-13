package nl.rhaydus.softcover.core.personal.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.Instant
import nl.rhaydus.softcover.core.database.model.ReadingSessionEntity
import nl.rhaydus.softcover.core.domain.model.ReadingSession
import nl.rhaydus.softcover.core.personal.data.datasource.ReadingSessionLocalDataSource
import nl.rhaydus.softcover.core.personal.data.mapper.toDomain
import nl.rhaydus.softcover.core.personal.data.mapper.toEntity
import nl.rhaydus.softcover.core.personal.domain.repository.ReadingSessionRepository

internal class ReadingSessionRepositoryImpl(
    private val localDataSource: ReadingSessionLocalDataSource,
) : ReadingSessionRepository {
    override fun observeByBookId(bookId: Int): Flow<List<ReadingSession>> =
        localDataSource.observeByBookId(bookId = bookId).map { list -> list.map { it.toDomain() } }

    override fun observeAll(): Flow<List<ReadingSession>> =
        localDataSource.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeActive(): Flow<ReadingSession?> =
        localDataSource.observeActive().map { it?.toDomain() }

    override fun observeById(id: Long): Flow<ReadingSession?> =
        localDataSource.observeById(id = id).map { it?.toDomain() }

    override suspend fun start(
        bookId: Int,
        startPage: Int?,
        startSeconds: Int?,
    ): Long = localDataSource.insert(
            entity = ReadingSessionEntity(
                bookId = bookId,
                startedAt = Clock.System.now().toString(),
                endedAt = null,
                startPage = startPage,
                endPage = null,
                startSeconds = startSeconds,
                endSeconds = null,
            ),
        )

    override suspend fun stop(
        id: Long,
        endPage: Int?,
        endSeconds: Int?,
    ) {
        val existing = localDataSource.observeById(id = id).firstOrNull() ?: return

        val now = Clock.System.now()

        val foldedPausedSeconds = existing.pausedSeconds + existing.openPauseSeconds(now = now)

        localDataSource.update(
            entity = existing.copy(
                endedAt = now.toString(),
                endPage = endPage,
                endSeconds = endSeconds,
                pausedSeconds = foldedPausedSeconds,
                lastPausedAt = null,
            ),
        )
    }

    override suspend fun pause(id: Long) {
        val existing = localDataSource.observeById(id = id).firstOrNull() ?: return

        if (existing.endedAt != null || existing.lastPausedAt != null) return

        localDataSource.update(
            entity = existing.copy(lastPausedAt = Clock.System.now().toString()),
        )
    }

    override suspend fun resume(id: Long) {
        val existing = localDataSource.observeById(id = id).firstOrNull() ?: return

        if (existing.lastPausedAt == null) return

        localDataSource.update(
            entity = existing.copy(
                pausedSeconds = existing.pausedSeconds + existing.openPauseSeconds(now = Clock.System.now()),
                lastPausedAt = null,
            ),
        )
    }

    override suspend fun update(session: ReadingSession) {
        localDataSource.update(entity = session.toEntity())
    }

    override suspend fun delete(id: Long) {
        localDataSource.delete(id = id)
    }
}

/** Seconds elapsed in the currently-open pause (since [lastPausedAt]), or 0 when running. */
private fun ReadingSessionEntity.openPauseSeconds(now: Instant): Int {
    val pausedAt = lastPausedAt ?: return 0

    val seconds = runCatching { (now - Instant.parse(pausedAt)).inWholeSeconds }
        .getOrDefault(0L)

    return seconds.coerceIn(
        0L,
        Int.MAX_VALUE.toLong(),
    ).toInt()
}

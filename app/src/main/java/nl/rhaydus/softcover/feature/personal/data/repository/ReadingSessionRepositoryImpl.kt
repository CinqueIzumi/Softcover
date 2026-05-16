package nl.rhaydus.softcover.feature.personal.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.personal.data.datasource.ReadingSessionLocalDataSource
import nl.rhaydus.softcover.feature.personal.data.mapper.toDomain
import nl.rhaydus.softcover.feature.personal.data.mapper.toEntity
import nl.rhaydus.softcover.feature.personal.data.model.ReadingSessionEntity
import nl.rhaydus.softcover.feature.personal.domain.model.ReadingSession
import nl.rhaydus.softcover.feature.personal.domain.repository.ReadingSessionRepository
import java.time.Instant

class ReadingSessionRepositoryImpl(
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
                startedAt = Instant.now().toString(),
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
        val existing = localDataSource.observeById(id = id).first() ?: return

        localDataSource.update(
            entity = existing.copy(
                endedAt = Instant.now().toString(),
                endPage = endPage,
                endSeconds = endSeconds,
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

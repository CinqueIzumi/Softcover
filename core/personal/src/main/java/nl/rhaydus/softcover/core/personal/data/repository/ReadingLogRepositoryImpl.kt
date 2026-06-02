package nl.rhaydus.softcover.core.personal.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.database.model.ReadingLogEntryEntity
import nl.rhaydus.softcover.core.personal.data.datasource.ReadingLogLocalDataSource
import nl.rhaydus.softcover.core.personal.data.mapper.toDomain
import nl.rhaydus.softcover.core.personal.data.mapper.toEntity
import nl.rhaydus.softcover.core.personal.domain.model.ReadingLogEntry
import nl.rhaydus.softcover.core.personal.domain.repository.ReadingLogRepository
import java.time.Instant
import java.time.LocalDate

internal class ReadingLogRepositoryImpl(
    private val localDataSource: ReadingLogLocalDataSource,
) : ReadingLogRepository {
    override fun observeByBookId(bookId: Int): Flow<List<ReadingLogEntry>> =
        localDataSource.observeByBookId(bookId = bookId).map { list -> list.map { it.toDomain() } }

    override fun observeCountByBookId(bookId: Int): Flow<Int> =
        localDataSource.observeCountByBookId(bookId = bookId)

    override suspend fun add(
        bookId: Int,
        startedAt: LocalDate?,
        finishedAt: LocalDate?,
        rating: Double?,
        note: String?,
    ): Long = localDataSource.insert(
        entity = ReadingLogEntryEntity(
            bookId = bookId,
            startedAt = startedAt?.toString(),
            finishedAt = finishedAt?.toString(),
            rating = rating,
            note = note,
            createdAt = Instant.now().toString(),
        ),
    )

    override suspend fun update(entry: ReadingLogEntry) {
        localDataSource.update(entity = entry.toEntity())
    }

    override suspend fun delete(id: Long) {
        localDataSource.delete(id = id)
    }
}

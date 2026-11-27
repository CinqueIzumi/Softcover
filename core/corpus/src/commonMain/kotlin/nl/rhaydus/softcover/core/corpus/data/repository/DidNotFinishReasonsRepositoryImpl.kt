package nl.rhaydus.softcover.core.corpus.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.corpus.data.datasource.DidNotFinishReasonsLocalDataSource
import nl.rhaydus.softcover.core.corpus.data.mapper.toDomain
import nl.rhaydus.softcover.core.corpus.data.mapper.toEntity
import nl.rhaydus.softcover.core.corpus.domain.repository.DidNotFinishReasonsRepository
import nl.rhaydus.softcover.core.domain.model.DidNotFinishReasons
import kotlin.time.Clock

internal class DidNotFinishReasonsRepositoryImpl(
    private val localDataSource: DidNotFinishReasonsLocalDataSource,
    private val clock: Clock = Clock.System,
) : DidNotFinishReasonsRepository {
    override fun observeAll(): Flow<Map<Int, DidNotFinishReasons>> =
        localDataSource.observeAll().map { entities -> entities.associate { it.bookId to it.toDomain() } }

    override fun observe(bookId: Int): Flow<DidNotFinishReasons> =
        localDataSource.observe(bookId = bookId).map { it?.toDomain() ?: DidNotFinishReasons(bookId = bookId) }

    override suspend fun get(bookId: Int): DidNotFinishReasons =
        localDataSource.get(bookId = bookId)?.toDomain() ?: DidNotFinishReasons(bookId = bookId)

    override suspend fun save(reasons: DidNotFinishReasons) {
        val normalized = reasons.copy(ownWords = reasons.ownWords?.trim()?.takeIf { it.isNotEmpty() })

        if (normalized.isEmpty) {
            localDataSource.delete(bookId = reasons.bookId)
        } else {
            localDataSource.upsert(entity = normalized.toEntity(updatedAt = clock.now().toEpochMilliseconds()))
        }
    }
}

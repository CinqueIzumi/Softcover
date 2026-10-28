package nl.rhaydus.softcover.core.corpus.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.corpus.data.datasource.BookTraitsLocalDataSource
import nl.rhaydus.softcover.core.corpus.data.mapper.toDomain
import nl.rhaydus.softcover.core.corpus.data.mapper.toEntity
import nl.rhaydus.softcover.core.corpus.domain.repository.BookTraitsRepository
import nl.rhaydus.softcover.core.domain.model.BookTraits
import kotlin.time.Clock

internal class BookTraitsRepositoryImpl(
    private val localDataSource: BookTraitsLocalDataSource,
    private val clock: Clock = Clock.System,
) : BookTraitsRepository {
    override fun observe(bookId: Int): Flow<BookTraits> =
        localDataSource.observe(bookId = bookId).map { it?.toDomain() ?: BookTraits(bookId = bookId) }

    override suspend fun get(bookId: Int): BookTraits =
        localDataSource.get(bookId = bookId)?.toDomain() ?: BookTraits(bookId = bookId)

    override suspend fun save(traits: BookTraits) {
        if (traits.isEmpty) {
            localDataSource.delete(bookId = traits.bookId)
        } else {
            localDataSource.upsert(entity = traits.toEntity(updatedAt = clock.now().toEpochMilliseconds()))
        }
    }
}

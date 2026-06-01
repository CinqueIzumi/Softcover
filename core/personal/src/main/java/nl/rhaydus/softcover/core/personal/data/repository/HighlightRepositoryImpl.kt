package nl.rhaydus.softcover.core.personal.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.data.database.model.HighlightEntity
import nl.rhaydus.softcover.core.personal.data.datasource.HighlightLocalDataSource
import nl.rhaydus.softcover.core.personal.data.mapper.toDomain
import nl.rhaydus.softcover.core.personal.data.mapper.toEntity
import nl.rhaydus.softcover.core.personal.domain.model.Highlight
import nl.rhaydus.softcover.core.personal.domain.repository.HighlightRepository
import java.time.Instant

class HighlightRepositoryImpl(
    private val localDataSource: HighlightLocalDataSource,
) : HighlightRepository {
    override fun observeByBookId(bookId: Int): Flow<List<Highlight>> =
        localDataSource.observeByBookId(bookId = bookId).map { list -> list.map { it.toDomain() } }

    override fun observeAll(): Flow<List<Highlight>> =
        localDataSource.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeById(id: Long): Flow<Highlight?> =
        localDataSource.observeById(id = id).map { it?.toDomain() }

    override suspend fun add(
        bookId: Int,
        quote: String,
        page: Int?,
        note: String?,
    ): Long = localDataSource.insert(
            entity = HighlightEntity(
                bookId = bookId,
                quote = quote,
                page = page,
                note = note,
                createdAt = Instant.now().toString(),
            ),
        )

    override suspend fun update(highlight: Highlight) {
        localDataSource.update(entity = highlight.toEntity())
    }

    override suspend fun delete(id: Long) {
        localDataSource.delete(id = id)
    }
}

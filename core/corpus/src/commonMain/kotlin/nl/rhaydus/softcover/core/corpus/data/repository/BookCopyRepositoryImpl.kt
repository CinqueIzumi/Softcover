package nl.rhaydus.softcover.core.corpus.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.corpus.data.datasource.BookCopyLocalDataSource
import nl.rhaydus.softcover.core.corpus.data.mapper.toDomain
import nl.rhaydus.softcover.core.corpus.data.mapper.toEntity
import nl.rhaydus.softcover.core.corpus.domain.repository.BookCopyRepository
import nl.rhaydus.softcover.core.domain.model.AcquisitionSource
import nl.rhaydus.softcover.core.domain.model.BookCopy
import kotlin.time.Clock

internal class BookCopyRepositoryImpl(
    private val localDataSource: BookCopyLocalDataSource,
    private val clock: Clock = Clock.System,
) : BookCopyRepository {
    override fun observe(bookId: Int): Flow<BookCopy> =
        localDataSource.observe(bookId = bookId).map { it?.toDomain() ?: BookCopy(bookId = bookId) }

    override suspend fun get(bookId: Int): BookCopy =
        localDataSource.get(bookId = bookId)?.toDomain() ?: BookCopy(bookId = bookId)

    override suspend fun save(copy: BookCopy) {
        val source = copy.source
        val isBlankCustom = source is AcquisitionSource.Other && source.text.isBlank()

        if (copy.isEmpty || isBlankCustom) {
            localDataSource.delete(bookId = copy.bookId)
        } else {
            localDataSource.upsert(entity = copy.toEntity(updatedAt = clock.now().toEpochMilliseconds()))
        }
    }
}

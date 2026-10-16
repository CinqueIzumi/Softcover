package nl.rhaydus.softcover.core.personal.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.database.model.QuoteEntity
import nl.rhaydus.softcover.core.domain.connectivity.QuoteWriteDrainer
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.softcover.core.domain.model.Quote
import nl.rhaydus.softcover.core.domain.model.QuotePosition
import nl.rhaydus.softcover.core.personal.data.datasource.QuoteLocalDataSource
import nl.rhaydus.softcover.core.personal.data.datasource.QuoteRemoteDataSource
import nl.rhaydus.softcover.core.personal.data.mapper.newPendingQuoteEntity
import nl.rhaydus.softcover.core.personal.data.mapper.toDomain
import nl.rhaydus.softcover.core.personal.domain.repository.QuoteRepository
import kotlin.time.Clock
import kotlin.time.Instant

internal class QuoteRepositoryImpl(
    private val localDataSource: QuoteLocalDataSource,
    private val remoteDataSource: QuoteRemoteDataSource,
    private val drainer: QuoteWriteDrainer,
    private val clock: Clock,
) : QuoteRepository {
    override fun observeForBook(bookId: Int): Flow<List<Quote>> =
        localDataSource.observeForBook(bookId = bookId).map { entities -> entities.map { it.toDomain() } }

    // Drained first so a queued insert or delete is on the server before the fetch that would otherwise
    // overwrite the local state.
    override suspend fun refreshForBook(bookId: Int) {
        drainer.drain()

        val remote: List<QuoteEntity> = remoteDataSource.getQuotes(bookId = bookId)

        localDataSource.replaceSyncedForBook(
            bookId = bookId,
            remote = remote,
        )
    }

    override fun observeAll(): Flow<List<Quote>> = localDataSource.observeAll()
        .map { entities -> entities.map { it.toDomain() } }
        .distinctUntilChanged()

    override suspend fun refreshAll() {
        drainer.drain()

        localDataSource.replaceAllSynced(remote = remoteDataSource.getAllQuotes())
    }

    override suspend fun add(
        bookId: Int,
        editionId: Int?,
        text: String,
        privacy: PrivacySetting,
        actionAt: Instant?,
        position: QuotePosition?,
        bookTitle: String?,
    ): Long {
        val now: Instant = clock.now()

        val localId: Long = localDataSource.insertWithWrite(
            entity = newPendingQuoteEntity(
                bookId = bookId,
                editionId = editionId,
                text = text,
                privacy = privacy,
                actionAt = actionAt ?: now,
                position = position,
                bookTitle = bookTitle,
            ),
            enqueuedAt = now.toString(),
        )

        drainer.drain()

        return localId
    }

    override suspend fun delete(localId: Long) {
        val queuedRemoteDelete: Boolean = localDataSource.remove(
            localId = localId,
            enqueuedAt = clock.now().toString(),
        )

        if (queuedRemoteDelete) drainer.drain()
    }
}

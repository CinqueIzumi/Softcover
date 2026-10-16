package nl.rhaydus.softcover.core.personal.data.datasource

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.database.dao.QuoteDao
import nl.rhaydus.softcover.core.database.model.QuoteEntity
import nl.rhaydus.softcover.core.database.model.QuoteSyncState

interface QuoteLocalDataSource {
    fun observeForBook(bookId: Int): Flow<List<QuoteEntity>>

    suspend fun getById(localId: Long): QuoteEntity?

    suspend fun insertWithWrite(
        entity: QuoteEntity,
        enqueuedAt: String,
    ): Long

    /** Returns whether a remote delete was queued. */
    suspend fun remove(
        localId: Long,
        enqueuedAt: String,
    ): Boolean

    suspend fun replaceSyncedForBook(
        bookId: Int,
        remote: List<QuoteEntity>,
    )

    /** Returns false when the local row no longer exists. */
    suspend fun markSynced(
        localId: Long,
        remoteId: Long,
    ): Boolean

    suspend fun restoreSynced(localId: Long)

    suspend fun deleteRow(localId: Long)
}

internal class QuoteLocalDataSourceImpl(
    private val dao: QuoteDao,
) : QuoteLocalDataSource {
    override fun observeForBook(bookId: Int): Flow<List<QuoteEntity>> = dao.observeForBook(bookId = bookId)

    override suspend fun getById(localId: Long): QuoteEntity? = dao.getById(localId = localId)

    override suspend fun insertWithWrite(
        entity: QuoteEntity,
        enqueuedAt: String,
    ): Long = dao.insertWithWrite(
        entity = entity,
        enqueuedAt = enqueuedAt,
    )

    override suspend fun remove(
        localId: Long,
        enqueuedAt: String,
    ): Boolean = dao.removeQuote(
        localId = localId,
        enqueuedAt = enqueuedAt,
    )

    override suspend fun replaceSyncedForBook(
        bookId: Int,
        remote: List<QuoteEntity>,
    ) = dao.replaceSyncedForBook(
        bookId = bookId,
        remote = remote,
    )

    override suspend fun markSynced(
        localId: Long,
        remoteId: Long,
    ) = dao.markSynced(
        localId = localId,
        remoteId = remoteId,
    )

    override suspend fun restoreSynced(localId: Long) = dao.setSyncState(
        localId = localId,
        state = QuoteSyncState.SYNCED,
    )

    override suspend fun deleteRow(localId: Long) = dao.deleteById(localId = localId)
}

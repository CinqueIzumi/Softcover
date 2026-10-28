package nl.rhaydus.softcover.core.connectivity.data.store

import nl.rhaydus.common.AppLog
import nl.rhaydus.offlinesync.PendingWrite
import nl.rhaydus.offlinesync.PendingWriteStore
import nl.rhaydus.softcover.core.connectivity.data.mapper.toEntity
import nl.rhaydus.softcover.core.connectivity.data.mapper.toPendingQuoteWrite
import nl.rhaydus.softcover.core.database.dao.PendingQuoteWriteDao
import nl.rhaydus.softcover.core.domain.connectivity.PendingQuoteWrite
import nl.rhaydus.softcover.core.domain.connectivity.PendingQuoteWriteKind
import nl.rhaydus.softcover.core.personal.data.datasource.QuoteLocalDataSource

/**
 * The Room-backed persistence seam for queued quote writes. The repository does not call [enqueue]: a quote
 * write must commit in the same transaction as its quote row, so `QuoteDao` inserts it directly.
 */
internal class PendingQuoteWriteStore(
    private val dao: PendingQuoteWriteDao,
    private val localDataSource: QuoteLocalDataSource,
) : PendingWriteStore<PendingQuoteWrite> {
    override suspend fun enqueue(payload: PendingQuoteWrite) {
        dao.insert(entity = payload.toEntity())
    }

    override suspend fun getPending(): List<PendingWrite<PendingQuoteWrite>> =
        dao.getPending().mapNotNull { entity ->
            val payload: PendingQuoteWrite? = entity.toPendingQuoteWrite()

            if (payload == null) {
                AppLog.e("Discarding unreplayable pending quote write: unknown kind '${entity.kind}'")

                dao.delete(localId = entity.localId)

                return@mapNotNull null
            }

            PendingWrite(
                localId = entity.localId,
                attempts = entity.attempts,
                payload = payload,
            )
        }

    override suspend fun delete(localId: Long) {
        dao.delete(localId = localId)
    }

    override suspend fun incrementAttempts(localId: Long) {
        dao.incrementAttempts(localId = localId)
    }

    override suspend fun abandon(write: PendingWrite<PendingQuoteWrite>) {
        val quoteLocalId: Long = write.payload.quoteLocalId

        when (write.payload.kind) {
            PendingQuoteWriteKind.INSERT -> localDataSource.deleteRow(localId = quoteLocalId)

            PendingQuoteWriteKind.DELETE -> {
                val hasRemoteCopy: Boolean = localDataSource.getById(localId = quoteLocalId)?.remoteId != null

                if (hasRemoteCopy) {
                    localDataSource.restoreSynced(localId = quoteLocalId)
                } else {
                    localDataSource.deleteRow(localId = quoteLocalId)
                }
            }
        }

        dao.delete(localId = write.localId)
    }
}

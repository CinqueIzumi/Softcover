package nl.rhaydus.softcover.core.connectivity.data.store

import nl.rhaydus.common.AppLog
import nl.rhaydus.offlinesync.PendingWrite
import nl.rhaydus.offlinesync.PendingWriteStore
import nl.rhaydus.softcover.core.connectivity.data.mapper.toEntity
import nl.rhaydus.softcover.core.connectivity.data.mapper.toPendingUserBookWrite
import nl.rhaydus.softcover.core.database.dao.PendingUserBookWriteDao
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWrite
import nl.rhaydus.softcover.core.domain.connectivity.UserBookWriteQueue

/**
 * Backs the foundation drainer's persistence seam with Room. Also *is* the enqueue side
 * ([UserBookWriteQueue]) — `PendingWriteStore<P>` extends `WriteQueue<P>`, so one type owns the table.
 *
 * The unique `(userBookId, kind)` index plus `insertReplacing` is what coalesces repeated writes of the
 * same kind for the same book into one queued row.
 */
internal class PendingUserBookWriteStore(
    private val dao: PendingUserBookWriteDao,
) : PendingWriteStore<PendingUserBookWrite>, UserBookWriteQueue {
    override suspend fun enqueue(payload: PendingUserBookWrite) {
        dao.insertReplacing(entity = payload.toEntity())
    }

    override suspend fun getPending(maxAttempts: Int): List<PendingWrite<PendingUserBookWrite>> =
        dao.getPending(maxAttempts = maxAttempts).mapNotNull { entity ->
            val payload: PendingUserBookWrite? = entity.toPendingUserBookWrite()

            if (payload == null) {
                // A row whose kind we cannot parse can never be replayed, so retrying it forever only
                // keeps it in the table. Drop it rather than let it accumulate behind the poison cap.
                AppLog.e("Discarding unreplayable pending user-book write: unknown kind '${entity.kind}'")

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
}

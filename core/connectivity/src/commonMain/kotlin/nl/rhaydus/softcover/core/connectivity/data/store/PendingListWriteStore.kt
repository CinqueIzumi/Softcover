package nl.rhaydus.softcover.core.connectivity.data.store

import nl.rhaydus.common.AppLog
import nl.rhaydus.offlinesync.PendingWrite
import nl.rhaydus.offlinesync.PendingWriteStore
import nl.rhaydus.softcover.core.connectivity.data.mapper.toEntity
import nl.rhaydus.softcover.core.connectivity.data.mapper.toPendingListWrite
import nl.rhaydus.softcover.core.database.dao.PendingListWriteDao
import nl.rhaydus.softcover.core.domain.connectivity.ListWriteQueue
import nl.rhaydus.softcover.core.domain.connectivity.PendingListWrite

/** The Room-backed persistence seam for queued list writes. See [PendingUserBookWriteStore]. */
internal class PendingListWriteStore(
    private val dao: PendingListWriteDao,
) : PendingWriteStore<PendingListWrite>, ListWriteQueue {
    override suspend fun enqueue(payload: PendingListWrite) {
        dao.insert(entity = payload.toEntity())
    }

    override suspend fun getPending(maxAttempts: Int): List<PendingWrite<PendingListWrite>> =
        dao.getPending(maxAttempts = maxAttempts).mapNotNull { entity ->
            val payload: PendingListWrite? = entity.toPendingListWrite()

            if (payload == null) {
                AppLog.e("Discarding unreplayable pending list write: unknown kind '${entity.kind}'")

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

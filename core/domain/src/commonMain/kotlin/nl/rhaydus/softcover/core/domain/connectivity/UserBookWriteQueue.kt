package nl.rhaydus.softcover.core.domain.connectivity

import nl.rhaydus.offlinesync.WriteQueue

/** Names the foundation queue for Softcover's user-book payload, so Koin can bind it past type erasure. */
interface UserBookWriteQueue : WriteQueue<PendingUserBookWrite> {
    /** Whether any queued write is still eligible for replay, i.e. has not hit the drainer's poison cap. */
    suspend fun hasPending(): Boolean
}

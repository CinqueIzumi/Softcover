package nl.rhaydus.softcover.core.domain.connectivity

import nl.rhaydus.offlinesync.WriteQueue

/** Names the foundation queue for Softcover's user-book payload, so Koin can bind it past type erasure. */
interface UserBookWriteQueue : WriteQueue<PendingUserBookWrite> {
    /** Whether any queued write is still eligible for replay, i.e. has not hit the drainer's poison cap. */
    suspend fun hasPending(): Boolean

    /** Like `enqueue`, replacing the `(userBookId, kind)` row, but returns the new row's local id. */
    suspend fun append(write: PendingUserBookWrite): Long

    /** Deletes exactly the row with [localId]; a no-op when it is already gone. */
    suspend fun remove(localId: Long)

    /** Whether the stored row for [write]'s `(userBookId, kind)` is equal to [write], ignoring attempts. */
    suspend fun isCurrent(write: PendingUserBookWrite): Boolean

    /** The kinds still eligible for replay, per user book; rows at the poison cap are left out. */
    suspend fun pendingKindsByUserBook(): Map<Int, Set<PendingUserBookWriteKind>>

    /** Whether another replayable row for [bookId] was enqueued before the row [exceptLocalId]. */
    suspend fun hasOlderPending(
        bookId: Int,
        exceptLocalId: Long,
    ): Boolean
}

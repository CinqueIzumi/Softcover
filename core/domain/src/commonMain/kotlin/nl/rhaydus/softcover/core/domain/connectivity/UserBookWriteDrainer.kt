package nl.rhaydus.softcover.core.domain.connectivity

import kotlinx.coroutines.CoroutineScope

interface UserBookWriteDrainer {
    fun start(scope: CoroutineScope)

    /**
     * Replays every queued offline write and reports, per affected `userBook` id, which kinds of
     * write were successfully synced to the server. Callers use the kinds to preserve only the
     * fields each replayed write actually owns when reconciling against a fresh server fetch — a
     * field the server changed underneath (and that no pending write touched) must not be clobbered
     * by the local optimistic copy.
     */
    suspend fun drainPendingUpdates(): Map<Int, Set<PendingUserBookWriteKind>>
}

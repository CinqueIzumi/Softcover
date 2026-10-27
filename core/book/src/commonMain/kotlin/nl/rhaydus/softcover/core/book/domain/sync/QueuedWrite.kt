package nl.rhaydus.softcover.core.book.domain.sync

import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWrite

/** A user-book write appended to the offline queue, identified by its queue row [localId]. */
data class QueuedWrite(
    val localId: Long,
    val write: PendingUserBookWrite,
)

package nl.rhaydus.softcover.core.domain.connectivity

import nl.rhaydus.offlinesync.WriteQueue

/** Names the foundation queue for Softcover's user-book payload, so Koin can bind it past type erasure. */
interface UserBookWriteQueue : WriteQueue<PendingUserBookWrite>

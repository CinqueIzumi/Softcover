package nl.rhaydus.softcover.core.domain.connectivity

import nl.rhaydus.offlinesync.WriteQueue

/** Names the foundation queue for Softcover's list payload, so Koin can bind it past type erasure. */
interface ListWriteQueue : WriteQueue<PendingListWrite>

package nl.rhaydus.softcover.core.domain.connectivity

interface ListWriteQueue {
    suspend fun enqueue(write: PendingListWrite)
}

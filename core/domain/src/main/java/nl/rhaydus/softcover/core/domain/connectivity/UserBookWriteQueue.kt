package nl.rhaydus.softcover.core.domain.connectivity

interface UserBookWriteQueue {
    suspend fun enqueue(update: PendingUserBookWrite)
}

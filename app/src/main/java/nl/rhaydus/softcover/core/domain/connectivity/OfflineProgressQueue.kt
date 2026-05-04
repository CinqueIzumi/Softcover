package nl.rhaydus.softcover.core.domain.connectivity

interface OfflineProgressQueue {
    suspend fun enqueue(update: PendingProgressUpdate)
}

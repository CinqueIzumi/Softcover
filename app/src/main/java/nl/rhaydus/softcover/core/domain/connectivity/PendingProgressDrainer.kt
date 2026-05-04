package nl.rhaydus.softcover.core.domain.connectivity

interface PendingProgressDrainer {
    suspend fun drainPendingUpdates(): Set<Int>
}

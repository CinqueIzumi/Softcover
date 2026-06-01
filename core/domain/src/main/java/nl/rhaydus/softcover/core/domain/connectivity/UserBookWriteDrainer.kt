package nl.rhaydus.softcover.core.domain.connectivity

interface UserBookWriteDrainer {
    suspend fun drainPendingUpdates(): Set<Int>
}

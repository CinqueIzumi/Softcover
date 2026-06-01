package nl.rhaydus.softcover.core.domain.connectivity

interface ListWriteDrainer {
    suspend fun drainPendingWrites()
}

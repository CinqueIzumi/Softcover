package nl.rhaydus.softcover.core.domain.connectivity

import kotlinx.coroutines.flow.StateFlow

interface NetworkAvailabilityProvider {
    val isOnline: StateFlow<Boolean>

    suspend fun awaitOnline()
}

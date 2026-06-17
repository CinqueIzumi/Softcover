package nl.rhaydus.softcover.core.domain.connectivity

import kotlin.concurrent.Volatile

object NetworkAvailability {
    @Volatile
    private var providerRef: NetworkAvailabilityProvider? = null

    fun install(provider: NetworkAvailabilityProvider) {
        providerRef = provider
    }

    fun isOnline(): Boolean = providerRef?.isOnline?.value ?: true
}

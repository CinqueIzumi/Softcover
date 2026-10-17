package nl.rhaydus.softcover.core.presentation.navigation

import kotlinx.coroutines.flow.StateFlow

/**
 * Holds the one navigation request an external entry point has made but the UI has not yet acted on.
 * Held as state, not a one-shot event, so a cold start survives until the shell is composed.
 */
interface ExternalNavigationRequests {
    val pending: StateFlow<ExternalNavigationRequest?>

    fun request(request: ExternalNavigationRequest)

    /** Clears [request] only if it is still the pending one, so a newer request is never lost. */
    fun consume(request: ExternalNavigationRequest)

    fun clear()
}

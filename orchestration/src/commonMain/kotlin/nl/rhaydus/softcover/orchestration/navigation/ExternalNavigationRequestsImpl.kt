package nl.rhaydus.softcover.orchestration.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import nl.rhaydus.softcover.core.presentation.navigation.ExternalNavigationRequest
import nl.rhaydus.softcover.core.presentation.navigation.ExternalNavigationRequests

internal class ExternalNavigationRequestsImpl : ExternalNavigationRequests {
    private val _pending = MutableStateFlow<ExternalNavigationRequest?>(null)

    override val pending: StateFlow<ExternalNavigationRequest?> = _pending.asStateFlow()

    override fun request(request: ExternalNavigationRequest) {
        _pending.value = request
    }

    override fun consume(request: ExternalNavigationRequest) {
        _pending.update { current -> if (current == request) null else current }
    }

    override fun clear() {
        _pending.value = null
    }
}

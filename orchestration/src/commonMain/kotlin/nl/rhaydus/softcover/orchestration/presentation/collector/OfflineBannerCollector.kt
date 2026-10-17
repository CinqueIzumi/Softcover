package nl.rhaydus.softcover.orchestration.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.orchestration.presentation.event.RootEvent
import nl.rhaydus.softcover.orchestration.presentation.screenmodel.RootDependencies
import nl.rhaydus.softcover.orchestration.presentation.state.RootLocalVariables
import nl.rhaydus.softcover.orchestration.presentation.state.RootUiState
import nl.rhaydus.toad.ActionScope

internal class OfflineBannerCollector : RootCollector {
    override suspend fun onLaunch(
        scope: ActionScope<RootUiState, RootEvent, RootLocalVariables>,
        dependencies: RootDependencies,
    ) {
        val offlineBanner = dependencies.offlineCopy.banner()

        dependencies.networkAvailabilityProvider.isOnline.collectLatest { isOnline ->
            scope.setState { it.copy(offlineBanner = offlineBanner.copy(visible = isOnline.not())) }
        }
    }
}

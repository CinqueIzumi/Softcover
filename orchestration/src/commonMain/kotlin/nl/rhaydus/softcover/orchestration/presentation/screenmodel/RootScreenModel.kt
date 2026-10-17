package nl.rhaydus.softcover.orchestration.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.platform.NetworkAvailabilityProvider
import nl.rhaydus.softcover.core.component.state.OfflineCopy
import nl.rhaydus.softcover.orchestration.presentation.collector.RootCollector
import nl.rhaydus.softcover.orchestration.presentation.event.RootEvent
import nl.rhaydus.softcover.orchestration.presentation.state.RootLocalVariables
import nl.rhaydus.softcover.orchestration.presentation.state.RootUiState
import nl.rhaydus.toad.ToadScreenModel

internal class RootScreenModel(
    networkAvailabilityProvider: NetworkAvailabilityProvider,
    offlineCopy: OfflineCopy,
    flows: List<RootCollector>,
    appDispatchers: AppDispatchers,
) : ToadScreenModel<RootUiState, RootEvent, RootDependencies, RootCollector, RootLocalVariables>(
    initialState = RootUiState(),
    initialLocalVariables = RootLocalVariables,
    initializers = flows,
) {
    override val dependencies = RootDependencies(
        networkAvailabilityProvider = networkAvailabilityProvider,
        offlineCopy = offlineCopy,
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
    )

    init {
        startInitializers()
    }
}

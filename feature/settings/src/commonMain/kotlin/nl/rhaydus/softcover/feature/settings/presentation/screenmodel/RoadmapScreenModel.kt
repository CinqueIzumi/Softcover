package nl.rhaydus.softcover.feature.settings.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.feature.settings.domain.usecase.ObserveRoadmapUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.RefreshRoadmapUseCase
import nl.rhaydus.softcover.feature.settings.presentation.action.RoadmapAction
import nl.rhaydus.softcover.feature.settings.presentation.collector.RoadmapCollector
import nl.rhaydus.softcover.feature.settings.presentation.event.RoadmapEvent
import nl.rhaydus.softcover.feature.settings.presentation.state.RoadmapLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.RoadmapUiState
import nl.rhaydus.toad.ToadScreenModel

internal class RoadmapScreenModel(
    observeRoadmapUseCase: ObserveRoadmapUseCase,
    refreshRoadmapUseCase: RefreshRoadmapUseCase,
    appDispatchers: AppDispatchers,
    flows: List<RoadmapCollector>,
) : ToadScreenModel<
    RoadmapUiState,
    RoadmapEvent,
    RoadmapDependencies,
    RoadmapCollector,
    RoadmapLocalVariables,
    >(
    initialState = RoadmapUiState(),
    initialLocalVariables = RoadmapLocalVariables(),
    initializers = flows,
) {
    override val dependencies = RoadmapDependencies(
        observeRoadmapUseCase = observeRoadmapUseCase,
        refreshRoadmapUseCase = refreshRoadmapUseCase,
        mainDispatcher = appDispatchers.main,
        coroutineScope = screenModelScope,
    )

    init {
        startInitializers()
    }

    fun runAction(action: RoadmapAction) = dispatch(action = action)
}

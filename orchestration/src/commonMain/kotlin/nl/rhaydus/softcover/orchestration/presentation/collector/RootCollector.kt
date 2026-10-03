package nl.rhaydus.softcover.orchestration.presentation.collector

import nl.rhaydus.softcover.orchestration.presentation.event.RootEvent
import nl.rhaydus.softcover.orchestration.presentation.screenmodel.RootDependencies
import nl.rhaydus.softcover.orchestration.presentation.state.RootLocalVariables
import nl.rhaydus.softcover.orchestration.presentation.state.RootUiState
import nl.rhaydus.toad.Collector

internal sealed interface RootCollector : Collector<
        RootUiState,
        RootEvent,
        RootDependencies,
        RootLocalVariables,
        >

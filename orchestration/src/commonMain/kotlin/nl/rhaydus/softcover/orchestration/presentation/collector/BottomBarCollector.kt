package nl.rhaydus.softcover.orchestration.presentation.collector

import nl.rhaydus.softcover.orchestration.presentation.event.BottomBarEvent
import nl.rhaydus.softcover.orchestration.presentation.screenmodel.BottomBarDependencies
import nl.rhaydus.softcover.orchestration.presentation.state.BottomBarLocalVariables
import nl.rhaydus.softcover.orchestration.presentation.state.BottomBarUiState
import nl.rhaydus.toad.Collector

internal sealed interface BottomBarCollector : Collector<
        BottomBarUiState,
        BottomBarEvent,
        BottomBarDependencies,
        BottomBarLocalVariables,
        >

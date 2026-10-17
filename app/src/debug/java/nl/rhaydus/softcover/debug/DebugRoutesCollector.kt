package nl.rhaydus.softcover.debug

import nl.rhaydus.toad.Collector

sealed interface DebugRoutesCollector : Collector<
        DebugRoutesUiState,
        DebugRoutesEvent,
        DebugRoutesDependencies,
        DebugRoutesLocalVariables,
        >

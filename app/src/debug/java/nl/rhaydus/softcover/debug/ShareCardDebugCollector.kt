package nl.rhaydus.softcover.debug

import nl.rhaydus.toad.Collector

sealed interface ShareCardDebugCollector : Collector<
        ShareCardDebugUiState,
        ShareCardDebugEvent,
        ShareCardDebugDependencies,
        ShareCardDebugLocalVariables,
        >

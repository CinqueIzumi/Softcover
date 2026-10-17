package nl.rhaydus.softcover.debug

import nl.rhaydus.toad.Collector

sealed interface MotionDebugCollector : Collector<
        MotionDebugUiState,
        MotionDebugEvent,
        MotionDebugDependencies,
        MotionDebugLocalVariables,
        >

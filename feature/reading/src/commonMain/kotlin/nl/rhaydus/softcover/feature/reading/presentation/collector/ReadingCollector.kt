package nl.rhaydus.softcover.feature.reading.presentation.collector

import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.Collector

internal sealed interface ReadingCollector : Collector<
        ReadingScreenUiState,
        ReadingScreenEvent,
        ReadingScreenDependencies,
        ReadingLocalVariables,
        >

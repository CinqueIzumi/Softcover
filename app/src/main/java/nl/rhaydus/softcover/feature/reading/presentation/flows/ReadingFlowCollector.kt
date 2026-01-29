package nl.rhaydus.softcover.feature.reading.presentation.flows

import nl.rhaydus.softcover.core.presentation.toad.FlowCollector
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.softcover.feature.reading.presentation.viewmodel.ReadingScreenDependencies

sealed interface ReadingFlowCollector : FlowCollector<
        ReadingScreenUiState,
        ReadingScreenEvent,
        ReadingScreenDependencies,
        ReadingLocalVariables,
        >
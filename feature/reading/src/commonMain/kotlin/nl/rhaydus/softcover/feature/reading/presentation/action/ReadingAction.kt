package nl.rhaydus.softcover.feature.reading.presentation.action

import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.UiAction

internal sealed interface ReadingAction : UiAction<
        ReadingScreenDependencies,
        ReadingScreenUiState,
        ReadingScreenEvent,
        ReadingLocalVariables,
        >

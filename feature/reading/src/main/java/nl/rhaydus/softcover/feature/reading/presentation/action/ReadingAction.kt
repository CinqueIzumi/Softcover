package nl.rhaydus.softcover.feature.reading.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.toad.UiAction
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState

sealed interface ReadingAction : UiAction<
        ReadingScreenDependencies,
        ReadingScreenUiState,
        ReadingScreenEvent,
        ReadingLocalVariables,
        >
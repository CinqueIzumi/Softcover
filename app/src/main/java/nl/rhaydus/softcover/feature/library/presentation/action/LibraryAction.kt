package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.core.presentation.toad.UiAction
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies

sealed interface LibraryAction : UiAction<
        LibraryDependencies,
        LibraryUiState,
        LibraryEvent,
        LibraryLocalVariables,
        >
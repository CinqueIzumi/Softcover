package nl.rhaydus.softcover.feature.library.presentation.flows

import nl.rhaydus.softcover.core.presentation.toad.Initializer
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies

sealed interface LibraryInitializer : Initializer<
        LibraryUiState,
        LibraryEvent,
        LibraryDependencies,
        LibraryLocalVariables,
        >
package nl.rhaydus.softcover.feature.library.presentation.flows

import nl.rhaydus.softcover.core.presentation.toad.FlowCollector
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.viewmodel.LibraryDependencies

sealed interface LibraryFlowCollector :
    FlowCollector<LibraryUiState, LibraryEvent, LibraryDependencies>
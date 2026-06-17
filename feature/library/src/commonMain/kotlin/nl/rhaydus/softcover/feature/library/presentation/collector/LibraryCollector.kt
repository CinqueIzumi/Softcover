package nl.rhaydus.softcover.feature.library.presentation.collector

import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.Collector

internal sealed interface LibraryCollector : Collector<
        LibraryUiState,
        LibraryEvent,
        LibraryDependencies,
        LibraryLocalVariables,
        >

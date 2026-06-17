package nl.rhaydus.softcover.feature.library.presentation.flows

import nl.rhaydus.softcover.core.designsystem.presentation.toad.Collector
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

internal sealed interface LibraryInitializer : Collector<
        LibraryUiState,
        LibraryEvent,
        LibraryDependencies,
        LibraryLocalVariables,
        >

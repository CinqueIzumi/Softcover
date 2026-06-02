package nl.rhaydus.softcover.feature.reading.presentation.flows

import nl.rhaydus.softcover.core.designsystem.presentation.toad.Initializer
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState

sealed interface ReadingInitializer : Initializer<
        ReadingScreenUiState,
        ReadingScreenEvent,
        ReadingScreenDependencies,
        ReadingLocalVariables,
        >
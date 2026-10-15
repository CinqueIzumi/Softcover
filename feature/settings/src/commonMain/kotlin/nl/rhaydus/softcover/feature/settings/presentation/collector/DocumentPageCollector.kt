package nl.rhaydus.softcover.feature.settings.presentation.collector

import nl.rhaydus.softcover.feature.settings.presentation.event.DocumentPageEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.DocumentPageDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.DocumentPageLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.DocumentPageUiState
import nl.rhaydus.toad.Collector

internal sealed interface DocumentPageCollector : Collector<
        DocumentPageUiState,
        DocumentPageEvent,
        DocumentPageDependencies,
        DocumentPageLocalVariables,
        >

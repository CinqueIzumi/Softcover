package nl.rhaydus.softcover.feature.widget.presentation.collector

import nl.rhaydus.softcover.feature.widget.presentation.event.UpdateProgressEvent
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.UpdateProgressDependencies
import nl.rhaydus.softcover.feature.widget.presentation.state.UpdateProgressLocalVariables
import nl.rhaydus.softcover.feature.widget.presentation.state.UpdateProgressUiState
import nl.rhaydus.toad.Collector

internal sealed interface UpdateProgressCollector : Collector<
    UpdateProgressUiState,
    UpdateProgressEvent,
    UpdateProgressDependencies,
    UpdateProgressLocalVariables,
    >

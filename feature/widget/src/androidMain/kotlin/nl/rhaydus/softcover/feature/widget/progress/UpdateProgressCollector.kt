package nl.rhaydus.softcover.feature.widget.progress

import nl.rhaydus.toad.Collector

internal sealed interface UpdateProgressCollector : Collector<
    UpdateProgressUiState,
    UpdateProgressEvent,
    UpdateProgressDependencies,
    UpdateProgressLocalVariables,
    >

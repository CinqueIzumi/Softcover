package nl.rhaydus.softcover.feature.widget.progress

import nl.rhaydus.toad.UiAction

internal sealed interface UpdateProgressAction : UiAction<
    UpdateProgressDependencies,
    UpdateProgressUiState,
    UpdateProgressEvent,
    UpdateProgressLocalVariables,
    >

package nl.rhaydus.softcover.feature.widget.presentation.action

import nl.rhaydus.softcover.feature.widget.presentation.event.UpdateProgressEvent
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.UpdateProgressDependencies
import nl.rhaydus.softcover.feature.widget.presentation.state.UpdateProgressLocalVariables
import nl.rhaydus.softcover.feature.widget.presentation.state.UpdateProgressUiState
import nl.rhaydus.toad.UiAction

internal sealed interface UpdateProgressAction : UiAction<
    UpdateProgressDependencies,
    UpdateProgressUiState,
    UpdateProgressEvent,
    UpdateProgressLocalVariables,
    >

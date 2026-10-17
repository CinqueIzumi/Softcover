package nl.rhaydus.softcover.orchestration.presentation.state

import nl.rhaydus.softcover.core.component.state.EmptyStateUiModel
import nl.rhaydus.toad.UiState

internal data class BottomBarUiState(
    val emptyDetailPane: EmptyStateUiModel = EmptyStateUiModel(title = "Select a book to see its details."),
) : UiState

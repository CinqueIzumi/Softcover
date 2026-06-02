package nl.rhaydus.softcover.feature.scan.presentation.state

import nl.rhaydus.softcover.core.designsystem.presentation.toad.UiState

internal data class ScanUiState(
    val isResolving: Boolean = false,
    val unknownIsbn: String? = null,
    val isAddingBook: Boolean = false,
) : UiState

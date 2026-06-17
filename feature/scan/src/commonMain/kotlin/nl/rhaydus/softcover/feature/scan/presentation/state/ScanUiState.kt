package nl.rhaydus.softcover.feature.scan.presentation.state

import nl.rhaydus.toad.UiState

internal data class ScanUiState(
    val isResolving: Boolean = false,
    val unknownIsbn: String? = null,
    val isAddingBook: Boolean = false,
) : UiState

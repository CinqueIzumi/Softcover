package nl.rhaydus.softcover.feature.scan.presentation.state

import nl.rhaydus.softcover.core.presentation.toad.UiState

data class ScanUiState(
    val isResolving: Boolean = false,
    val unknownIsbn: String? = null,
    val isAddingBook: Boolean = false,
) : UiState

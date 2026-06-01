package nl.rhaydus.softcover.feature.lists.presentation.state

import nl.rhaydus.softcover.core.presentation.toad.UiState

data class CreateListUiState(
    val name: String = "",
    val isSubmitting: Boolean = false,
) : UiState {
    val canSubmit: Boolean
        get() = name.trim().isNotEmpty() && isSubmitting.not()
}

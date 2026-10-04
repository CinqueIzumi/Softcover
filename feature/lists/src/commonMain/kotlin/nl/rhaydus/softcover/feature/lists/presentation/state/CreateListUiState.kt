package nl.rhaydus.softcover.feature.lists.presentation.state

import nl.rhaydus.softcover.core.component.control.SegmentedControlUiModel
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.toad.UiState

internal data class CreateListUiState(
    val name: String = "",
    val privacy: PrivacySetting = PrivacySetting.PUBLIC,
    val privacyToggle: SegmentedControlUiModel<PrivacySetting> = privacyToggleFor(selected = PrivacySetting.PUBLIC),
    val isSubmitting: Boolean = false,
) : UiState {
    val canSubmit: Boolean
        get() = name.trim().isNotEmpty() && isSubmitting.not()
}

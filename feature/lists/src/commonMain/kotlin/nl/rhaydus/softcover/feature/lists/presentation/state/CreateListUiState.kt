package nl.rhaydus.softcover.feature.lists.presentation.state

import nl.rhaydus.softcover.core.component.control.SegmentedControlUiModel
import nl.rhaydus.softcover.core.component.field.TextFieldUiModel
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.toad.UiState

internal data class CreateListUiState(
    val privacy: PrivacySetting = PrivacySetting.PUBLIC,
    val privacyToggle: SegmentedControlUiModel<PrivacySetting> = privacyToggleFor(selected = PrivacySetting.PUBLIC),
    val isSubmitting: Boolean = false,
    val nameField: TextFieldUiModel = nameFieldFor(
        name = "",
        enabled = true,
    ),
) : UiState {
    val name: String
        get() = nameField.text

    val canSubmit: Boolean
        get() = name.trim().isNotEmpty() && isSubmitting.not()

    fun withName(newName: String): CreateListUiState = copy(nameField = nameField.copy(text = newName))

    fun withSubmitting(submitting: Boolean): CreateListUiState = copy(
        isSubmitting = submitting,
        nameField = nameField.copy(enabled = submitting.not()),
    )
}

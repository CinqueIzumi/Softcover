package nl.rhaydus.softcover.feature.lists.presentation.state

import nl.rhaydus.softcover.core.component.control.SegmentedControlUiModel
import nl.rhaydus.softcover.core.component.field.TextFieldUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.withModel
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.toad.UiState

internal data class CreateListUiState(
    val privacy: PrivacySetting = PrivacySetting.PUBLIC,
    val privacyToggle: SegmentedControlUiModel<PrivacySetting> = privacyToggleFor(selected = PrivacySetting.PUBLIC),
    val isSubmitting: Boolean = false,

    /** Open from the start: the screen is the sheet. */
    val sheet: SheetSession<SheetScaffoldUiModel, Nothing> = SheetSession.Open(
        model = createListSheetScaffold(
            canSubmit = false,
            submitting = false,
        ),
    ),
    val nameField: TextFieldUiModel = nameFieldFor(
        name = "",
        enabled = true,
    ),
) : UiState {
    val name: String
        get() = nameField.text

    val canSubmit: Boolean
        get() = name.trim().isNotEmpty() && isSubmitting.not()

    fun withName(newName: String): CreateListUiState = copy(
        nameField = nameField.copy(
            text = newName,
            error = null,
        ),
    ).withSheetFooter()

    fun withSubmitting(submitting: Boolean): CreateListUiState = copy(
        isSubmitting = submitting,
        nameField = nameField.copy(enabled = submitting.not()),
    ).withSheetFooter()

    private fun withSheetFooter(): CreateListUiState = copy(
        sheet = sheet.withModel {
            createListSheetScaffold(
                canSubmit = canSubmit,
                submitting = isSubmitting,
            )
        },
    )
}

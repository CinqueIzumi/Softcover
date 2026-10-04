package nl.rhaydus.softcover.orchestration.presentation

import nl.rhaydus.softcover.core.component.field.TextFieldUiModel
import nl.rhaydus.softcover.core.component.field.TextFieldVariant
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetFooterButton
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.presentation.state.ReAuthState

internal fun ReAuthState.toSheetUiModel(): ReAuthSheetUiModel = ReAuthSheetUiModel(
    scaffold = SheetScaffoldUiModel(
        header = SheetHeaderUiModel(
            section = SectionHeaderUiModel.Section(
                eyebrow = "Account",
                headline = "Session expired",
                description = "Your Hardcover API token was rejected. " +
                    "Paste a valid token below to continue without losing your local data.",
            ),
        ),
        footer = SheetFooterUiModel.Stacked(
            primary = SheetFooterButton(
                label = "Re-authenticate",
                enabled = apiKey.isNotBlank(),
            ),
            secondary = SheetFooterButton(label = "Log out"),
        ),
        dismissible = false,
        busy = isSubmitting,
        closeRequested = closeRequested,
    ),
    apiKeyField = TextFieldUiModel(
        key = "api-token",
        text = apiKey,
        placeholder = "Paste your Hardcover API token",
        variant = TextFieldVariant.Pill(),
        enabled = isSubmitting.not(),
    ),
    errorMessage = errorMessage,
)

package nl.rhaydus.softcover.orchestration.presentation

import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetFooterButton
import nl.rhaydus.softcover.core.component.sheet.SheetFooterLayout
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetLayout
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.presentation.state.ReAuthState

internal fun ReAuthState.toSheetUiModel(): ReAuthSheetUiModel = ReAuthSheetUiModel(
    scaffold = toScaffoldUiModel(),
    apiKey = apiKey,
    errorMessage = errorMessage,
    fieldEnabled = isSubmitting.not(),
)

private fun ReAuthState.toScaffoldUiModel(): SheetScaffoldUiModel = SheetScaffoldUiModel(
    header = SheetHeaderUiModel(
        section = SectionHeaderUiModel.Section(
            eyebrow = "Account",
            headline = "Session expired",
            description = "Your Hardcover API token was rejected. " +
                "Paste a valid token below to continue without losing your local data.",
        ),
    ),
    topAction = null,
    footer = SheetFooterUiModel(
        primary = SheetFooterButton(
            label = "Re-authenticate",
            enabled = apiKey.isNotBlank(),
            loading = isSubmitting,
        ),
        secondary = SheetFooterButton(
            label = "Log out",
            enabled = isSubmitting.not(),
        ),
        layout = SheetFooterLayout.Stacked,
    ),
    layout = SheetLayout.Scrolling,
    dismissible = false,
)

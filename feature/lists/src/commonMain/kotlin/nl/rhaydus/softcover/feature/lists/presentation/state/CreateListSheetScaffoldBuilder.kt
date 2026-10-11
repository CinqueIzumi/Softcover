package nl.rhaydus.softcover.feature.lists.presentation.state

import nl.rhaydus.softcover.core.component.sheet.SheetFooterButton
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

internal fun createListSheetScaffold(
    canSubmit: Boolean,
    submitting: Boolean,
): SheetScaffoldUiModel =
    SheetScaffoldUiModel(
        header = null,
        footer = SheetFooterUiModel.Stacked(
            primary = SheetFooterButton(
                label = "Create list",
                enabled = canSubmit,
                running = submitting,
            ),
            secondary = null,
        ),
    )

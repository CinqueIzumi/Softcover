package nl.rhaydus.softcover.feature.scan.presentation.state

import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetFooterButton
import nl.rhaydus.softcover.core.component.sheet.SheetFooterLayout
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetLayout
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

internal fun unknownIsbnSheetScaffoldFor(isAdding: Boolean): SheetScaffoldUiModel =
    SheetScaffoldUiModel(
        header = SheetHeaderUiModel(
            section = SectionHeaderUiModel.Section(
                eyebrow = "Not in Hardcover",
                headline = "We don't have this one yet.",
                description = "Double-check the ISBN below — if it's right, add it to Hardcover so you can track it.",
            ),
        ),
        topAction = null,
        footer = SheetFooterUiModel(
            primary = SheetFooterButton(
                label = if (isAdding) "Adding to Hardcover" else "Add to Hardcover",
                loading = isAdding,
            ),
            secondary = SheetFooterButton(
                label = "Cancel",
                enabled = isAdding.not(),
                dismissesSheet = true,
            ),
            layout = SheetFooterLayout.Stacked,
        ),
        layout = SheetLayout.Scrolling,
        dismissible = isAdding.not(),
    )

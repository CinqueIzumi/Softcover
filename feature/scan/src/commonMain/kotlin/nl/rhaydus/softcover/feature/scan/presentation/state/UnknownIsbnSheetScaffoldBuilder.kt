package nl.rhaydus.softcover.feature.scan.presentation.state

import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetFooterButton
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

internal fun unknownIsbnSheetScaffoldFor(isAdding: Boolean): SheetScaffoldUiModel =
    SheetScaffoldUiModel(
        header = SheetHeaderUiModel(
            section = SectionHeaderUiModel.Section(
                eyebrow = "Not in Hardcover",
                headline = "Hardcover doesn't have this one yet.",
                description = "Check the ISBN below. If it's right, add the book to Hardcover so you can track it.",
            ),
        ),
        footer = SheetFooterUiModel.Stacked(
            primary = SheetFooterButton(
                label = if (isAdding) "Adding to Hardcover" else "Add to Hardcover",
                running = isAdding,
            ),
            secondary = SheetFooterButton(
                label = "Cancel",
                dismissesSheet = true,
            ),
        ),
    )

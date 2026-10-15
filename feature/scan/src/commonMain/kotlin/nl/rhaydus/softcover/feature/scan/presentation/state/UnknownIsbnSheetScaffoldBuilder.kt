package nl.rhaydus.softcover.feature.scan.presentation.state

import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetFooterButton
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

internal fun unknownIsbnSheetScaffoldFor(
    isAdding: Boolean,
    isImportPending: Boolean,
): SheetScaffoldUiModel {
    val section = if (isImportPending && isAdding.not()) {
        SectionHeaderUiModel.Section(
            eyebrow = "Still importing",
            headline = "Hardcover is still fetching this one.",
            description = "It usually lands within a minute. Check again in a moment.",
        )
    } else {
        SectionHeaderUiModel.Section(
            eyebrow = "Not in Hardcover",
            headline = "Hardcover doesn't have this one yet.",
            description = if (isAdding) {
                "Hardcover fetches new books from its ISBN sources. It can take up to a minute."
            } else {
                "Check the ISBN below. If it's right, add the book to Hardcover so you can track it."
            },
        )
    }

    val idleLabel = if (isImportPending) "Check again" else "Add to Hardcover"

    return SheetScaffoldUiModel(
        header = SheetHeaderUiModel(section = section),
        footer = SheetFooterUiModel.Stacked(
            primary = SheetFooterButton(
                label = if (isAdding) "Adding to Hardcover" else idleLabel,
                running = isAdding,
            ),
            secondary = SheetFooterButton(
                label = "Cancel",
                dismissesSheet = true,
            ),
        ),
    )
}

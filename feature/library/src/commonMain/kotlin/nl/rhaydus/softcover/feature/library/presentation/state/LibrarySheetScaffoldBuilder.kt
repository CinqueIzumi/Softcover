package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetFooterButton
import nl.rhaydus.softcover.core.component.sheet.SheetFooterLayout
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderTrailing
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetLayout
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.feature.library.presentation.util.formatBookCount

internal fun arrangeSheetScaffoldFor(resultCount: Int): SheetScaffoldUiModel =
    SheetScaffoldUiModel(
        header = SheetHeaderUiModel(
            section = SectionHeaderUiModel.Section(
                eyebrow = "Arrange the shelf",
                headline = "Arrange this shelf.",
            ),
        ),
        topAction = null,
        footer = SheetFooterUiModel(
            primary = SheetFooterButton(
                label = "Show ${formatBookCount(count = resultCount)}",
                dismissesSheet = true,
            ),
            secondary = null,
            layout = SheetFooterLayout.Stacked,
        ),
        layout = SheetLayout.Scrolling,
    )

/**
 * The secondary "Clear all" is always present so the primary never shifts sideways when the first
 * filter is applied; [filtersActive] only enables it.
 */
internal fun filterSheetScaffoldFor(
    resultCount: Int,
    filtersActive: Boolean,
): SheetScaffoldUiModel =
    SheetScaffoldUiModel(
        header = SheetHeaderUiModel(
            section = SectionHeaderUiModel.Section(
                eyebrow = "Filter this shelf",
                headline = "Narrow this shelf.",
                description = "Combine facets to find the corner of your library you're after.",
            ),
        ),
        topAction = null,
        footer = SheetFooterUiModel(
            primary = SheetFooterButton(
                label = "Show ${formatBookCount(count = resultCount)}",
                dismissesSheet = true,
            ),
            secondary = SheetFooterButton(
                label = "Clear all",
                enabled = filtersActive,
            ),
            layout = SheetFooterLayout.Inline,
        ),
        layout = SheetLayout.Pinned,
    )

internal fun shelvesSheetScaffold(): SheetScaffoldUiModel =
    SheetScaffoldUiModel(
        header = SheetHeaderUiModel(
            section = SectionHeaderUiModel.Section(
                eyebrow = "Your shelves",
                headline = "Jump to a shelf",
            ),
            trailing = SheetHeaderTrailing.Action(label = "Manage"),
        ),
        topAction = null,
        footer = null,
        layout = SheetLayout.Scrolling,
    )

internal fun bulkRemoveSheetScaffoldFor(
    bookCount: Int,
    inProgress: Boolean,
): SheetScaffoldUiModel {
    val noun = if (bookCount == 1) "book" else "books"

    return SheetScaffoldUiModel(
        header = SheetHeaderUiModel(
            section = SectionHeaderUiModel.Section(
                eyebrow = "Library",
                headline = "Remove $bookCount $noun?",
                description = "They'll come off every shelf and out of your Hardcover library. " +
                    "You can always add them again later.",
            ),
        ),
        topAction = null,
        footer = SheetFooterUiModel(
            primary = SheetFooterButton(
                label = "Remove",
                loading = inProgress,
            ),
            secondary = SheetFooterButton(
                label = "Cancel",
                enabled = inProgress.not(),
                dismissesSheet = true,
            ),
            layout = SheetFooterLayout.Stacked,
        ),
        layout = SheetLayout.Scrolling,
        dismissible = inProgress.not(),
    )
}

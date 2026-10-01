package nl.rhaydus.softcover.feature.explore.presentation.state

import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetFooterButton
import nl.rhaydus.softcover.core.component.sheet.SheetFooterLayout
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetLayout
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.domain.model.Book

internal fun becauseYouReadGenreSheetScaffold(): SheetScaffoldUiModel =
    SheetScaffoldUiModel(
        header = SheetHeaderUiModel(
            section = SectionHeaderUiModel.Section(
                eyebrow = "Genre",
                headline = "Pick a genre",
            ),
        ),
        topAction = null,
        footer = null,
        layout = SheetLayout.Scrolling,
    )

internal fun continueSeriesMenuSheetScaffoldFor(book: Book): SheetScaffoldUiModel =
    SheetScaffoldUiModel(
        header = SheetHeaderUiModel(
            section = SectionHeaderUiModel.Section(
                eyebrow = book.bookSeries?.name ?: "Up next",
                headline = book.title,
            ),
        ),
        topAction = null,
        footer = SheetFooterUiModel(
            primary = null,
            secondary = SheetFooterButton(
                label = "Cancel",
                dismissesSheet = true,
            ),
            layout = SheetFooterLayout.Stacked,
        ),
        layout = SheetLayout.Scrolling,
    )

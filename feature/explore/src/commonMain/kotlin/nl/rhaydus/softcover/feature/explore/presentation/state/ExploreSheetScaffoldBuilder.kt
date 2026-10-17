package nl.rhaydus.softcover.feature.explore.presentation.state

import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetFooterButton
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
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
    )

internal fun continueSeriesMenuSheetScaffoldFor(book: Book): SheetScaffoldUiModel =
    SheetScaffoldUiModel(
        header = SheetHeaderUiModel(
            section = SectionHeaderUiModel.Section(
                eyebrow = book.bookSeries?.name ?: "Up next",
                headline = book.title,
            ),
        ),
        footer = SheetFooterUiModel.Stacked(
            primary = null,
            secondary = SheetFooterButton(
                label = "Cancel",
                dismissesSheet = true,
            ),
        ),
    )

package nl.rhaydus.softcover.feature.explore.presentation.mapper

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook
import nl.rhaydus.softcover.feature.explore.presentation.action.OnDismissContinueSeriesAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnDismissContinueSeriesBookAction
import nl.rhaydus.softcover.feature.explore.presentation.model.ContinueSeriesMenuOptionUiModel
import nl.rhaydus.softcover.feature.explore.presentation.model.ContinueSeriesMenuSheetUiModel
import nl.rhaydus.softcover.feature.explore.presentation.state.continueSeriesMenuSheetScaffoldFor

/** The up-next menu for [book]: hide just this title, plus hide the whole series when it belongs to one. */
internal fun continueSeriesMenuSheetFor(book: Book): ContinueSeriesMenuSheetUiModel {
    val authorText = book.authorString.takeIf { it.isNotBlank() }

    return ContinueSeriesMenuSheetUiModel(
        scaffold = continueSeriesMenuSheetScaffoldFor(book = book),
        options = buildList {
            add(
                ContinueSeriesMenuOptionUiModel(
                    icon = SoftcoverIcon.VisibilityOff,
                    label = "Hide this book",
                    description = "Just this title stops showing up here",
                    dismissAction = OnDismissContinueSeriesBookAction(
                        book = DismissedSeriesBook(
                            bookId = book.id,
                            title = book.title,
                            coverUrl = book.coverUrl,
                            authorText = authorText,
                            seriesName = book.bookSeries?.name,
                            seriesId = book.bookSeries?.id,
                            // The series cursor moves past this book's *last* position, so an omnibus
                            // spanning several positions can't re-match on the next fetch.
                            seriesPosition = book.positionsInSeries.lastOrNull(),
                        ),
                    ),
                ),
            )

            book.bookSeries?.let { series ->
                add(
                    ContinueSeriesMenuOptionUiModel(
                        icon = SoftcoverIcon.LibraryBooks,
                        label = "Hide everything from ${series.name}",
                        description = "Hide every book in this series from Up next",
                        dismissAction = OnDismissContinueSeriesAction(
                            seriesId = series.id,
                            seriesName = series.name,
                            coverUrl = book.coverUrl,
                            authorText = authorText,
                            bookCount = series.amountOfBooks,
                        ),
                    ),
                )
            }
        }.toImmutableList(),
    )
}

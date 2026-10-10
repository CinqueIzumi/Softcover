package nl.rhaydus.softcover.feature.explore.presentation.mapper

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.model.ContinueSeriesDismissalKind
import nl.rhaydus.softcover.feature.explore.presentation.model.ContinueSeriesMenuOptionUiModel
import nl.rhaydus.softcover.feature.explore.presentation.model.ContinueSeriesMenuSheetUiModel
import nl.rhaydus.softcover.feature.explore.presentation.state.continueSeriesMenuSheetScaffoldFor

/** The up-next menu for [book]: hide just this title, plus hide the whole series when it belongs to one. */
internal fun continueSeriesMenuSheetFor(book: Book): ContinueSeriesMenuSheetUiModel {
    return ContinueSeriesMenuSheetUiModel(
        scaffold = continueSeriesMenuSheetScaffoldFor(book = book),
        options = buildList {
            add(
                ContinueSeriesMenuOptionUiModel(
                    icon = SoftcoverIcon.VisibilityOff,
                    label = "Hide this book",
                    description = "Just this title stops showing up here",
                    kind = ContinueSeriesDismissalKind.THIS_BOOK,
                ),
            )

            book.bookSeries?.let { series ->
                add(
                    ContinueSeriesMenuOptionUiModel(
                        icon = SoftcoverIcon.LibraryBooks,
                        label = "Hide everything from ${series.name}",
                        description = "Hide every book in this series from Up next",
                        kind = ContinueSeriesDismissalKind.WHOLE_SERIES,
                    ),
                )
            }
        }.toImmutableList(),
    )
}

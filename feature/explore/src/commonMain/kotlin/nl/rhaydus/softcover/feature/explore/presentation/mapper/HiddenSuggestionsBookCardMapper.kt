package nl.rhaydus.softcover.feature.explore.presentation.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.bookcard.BookCardContent
import nl.rhaydus.softcover.core.component.bookcard.BookCardCover
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLead
import nl.rhaydus.softcover.core.component.bookcard.BookCardLinePart
import nl.rhaydus.softcover.core.component.bookcard.BookCardTrailing
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.uibinding.book.byline
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeries
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook

internal fun DismissedSeriesBook.toHiddenBookCard(cover: CoverUiModel): BookCardUiModel = BookCardUiModel(
    key = BookCardKey.Book(bookId = bookId),
    variant = BookCardVariant.Row(
        size = BookCardVariant.Row.Size.Compact,
        lead = BookCardLead.Cover(cover = BookCardCover(model = cover)),
        text = BookCardContent(
            title = title ?: "Hidden book",
            byline = byline(author = authorText),
        ),
        trailing = BookCardTrailing.Restore(label = "Restore"),
    ),
)

internal fun DismissedSeries.toHiddenSeriesCard(cover: CoverUiModel): BookCardUiModel = BookCardUiModel(
    key = BookCardKey.Series(seriesId = seriesId),
    variant = BookCardVariant.Row(
        size = BookCardVariant.Row.Size.Compact,
        lead = BookCardLead.SeriesStack(front = cover),
        text = BookCardContent(
            title = seriesName ?: "Hidden series",
            supportingLines = persistentListOf(scopeCaption()),
        ),
        trailing = BookCardTrailing.Restore(label = "Restore"),
    ),
)

private fun DismissedSeries.scopeCaption(): ImmutableList<BookCardLinePart> {
    val parts = listOfNotNull(
        authorText?.takeIf { it.isNotBlank() },
        bookCount?.let { if (it == 1) "1 book" else "$it books" },
    ).ifEmpty { listOf("Series") }

    return parts.map { BookCardLinePart.Text(text = it) }.toImmutableList()
}

package nl.rhaydus.softcover.feature.explore.presentation.mapper

import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.bookcard.BookCardContent
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLead
import nl.rhaydus.softcover.core.component.bookcard.BookCardLine
import nl.rhaydus.softcover.core.component.bookcard.BookCardLinePart
import nl.rhaydus.softcover.core.component.bookcard.BookCardTrailing
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeries
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook

internal fun DismissedSeriesBook.toHiddenBookCard(cover: CoverUiModel?): BookCardUiModel = BookCardUiModel(
    key = BookCardKey.Book(bookId = bookId),
    variant = BookCardVariant.Row(
        size = BookCardVariant.Row.Size.Compact,
        lead = cover?.let { BookCardLead.Cover(model = it) },
        text = BookCardContent(
            title = title ?: "Hidden book",
            byline = authorText?.takeIf { it.isNotBlank() }?.let { "By $it" },
        ),
        trailing = BookCardTrailing.Restore(label = "Restore"),
    ),
)

internal fun DismissedSeries.toHiddenSeriesCard(cover: CoverUiModel?): BookCardUiModel = BookCardUiModel(
    key = BookCardKey.Series(seriesId = seriesId),
    variant = BookCardVariant.Row(
        size = BookCardVariant.Row.Size.Compact,
        lead = BookCardLead.SeriesStack(front = cover),
        text = BookCardContent(
            title = seriesName ?: "Hidden series",
            supportingLines = persistentListOf(
                BookCardLine(parts = persistentListOf(BookCardLinePart.Text(text = scopeCaption()))),
            ),
        ),
        trailing = BookCardTrailing.Restore(label = "Restore"),
    ),
)

private fun DismissedSeries.scopeCaption(): String {
    val author = authorText?.takeIf { it.isNotBlank() }
    val countPart = bookCount?.let { if (it == 1) "1 book" else "$it books" }

    return when {
        author != null && countPart != null -> "$author · $countPart"
        author != null -> author
        countPart != null -> countPart
        else -> "Series"
    }
}

package nl.rhaydus.softcover.feature.library.presentation.screen.section

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.deadline.DeadlineSummaryUiModel
import nl.rhaydus.softcover.core.component.selection.SelectionMarkUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.presentation.prefetch.prefetchBookDetailOnPress

@Composable
internal fun LayoutBookEntry(
    book: Book,
    cover: CoverUiModel?,
    card: BookCardUiModel?,
    layout: LibraryGridLayout,
    onCardEvent: (BookCardEvent) -> Unit,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    selectionMark: SelectionMarkUiModel? = null,
    deadlineBadge: BadgeUiModel? = null,
    deadlineSummary: DeadlineSummaryUiModel? = null,
    dragHandle: (@Composable () -> Unit)? = null,
) {
    // The grid prefetches through the card's PressStarted event; only the rows need the modifier.
    // In selection mode the tap toggles selection, so skip the prefetch there.
    val entryModifier = if (selectionMark != null) modifier else modifier.prefetchBookDetailOnPress(book.id)

    val authorName = book.authors.map { it.name }.firstOrNull().orEmpty()

    val currentEdition = book.currentEdition

    // The wavy shelf-progress signature (redesign brief): a thin sine wave beside every
    // in-progress list row, independent of whether a deadline is tracked.
    val progressFraction = if (book.status == BookStatus.Reading) {
        book.userBookRead?.progress?.div(100f)
    } else {
        null
    }

    when (layout) {
        LibraryGridLayout.GRID_TWO_COLUMNS,
        LibraryGridLayout.GRID_THREE_COLUMNS,
        LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
        LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY,
            -> {
            if (card != null) {
                BookCard(
                    model = card,
                    onEvent = onCardEvent,
                    modifier = modifier,
                    content = dragHandle,
                )
            } else {
                // One frame before the collector lands: keep the cell's footprint and modifiers.
                Cover(
                    model = null,
                    modifier = modifier.fillMaxWidth(),
                )
            }
        }

        LibraryGridLayout.LIST_COMPACT -> {
            CompactRow(
                modifier = entryModifier,
                title = book.title,
                authorName = authorName,
                onClick = onClick,
                onLongClick = onLongClick,
                selectionMark = selectionMark,
                deadlineBadge = deadlineBadge,
                trailing = dragHandle,
            )
        }

        LibraryGridLayout.LIST_LARGE -> {
            LargeRow(
                modifier = entryModifier,
                title = book.title,
                authorName = currentEdition?.authorString.orEmpty(),
                onClick = onClick,
                onLongClick = onLongClick,
                seriesText = book.seriesText,
                releaseYear = book.releaseYear,
                usersCount = book.usersCount,
                rating = book.rating,
                progressFraction = progressFraction,
                deadlineSummary = deadlineSummary,
                trailing = dragHandle,
            ) { coverModifier ->
                SelectableCover(
                    modifier = coverModifier,
                    selectionMark = selectionMark,
                ) {
                    Cover(
                        model = cover,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

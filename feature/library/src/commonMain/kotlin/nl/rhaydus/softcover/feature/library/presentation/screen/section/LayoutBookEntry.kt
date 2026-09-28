package nl.rhaydus.softcover.feature.library.presentation.screen.section

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.badge.CoverOverlay
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.badge.DeadlineSummaryUiModel
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.selection.SelectionMarkUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.presentation.prefetch.prefetchBookDetailOnPress

@Composable
internal fun LayoutBookEntry(
    book: Book,
    cover: CoverUiModel?,
    layout: LibraryGridLayout,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    deadlineCountdownOverlay: CoverOverlayUiModel? = null,
    selectionMark: SelectionMarkUiModel? = null,
    deadlineBadge: BadgeUiModel? = null,
    deadlineSummary: DeadlineSummaryUiModel? = null,
    dragHandle: (@Composable () -> Unit)? = null,
) {
    // Prefetch only makes sense when the tap opens book detail. In selection mode the tap
    // toggles selection, so skip the prefetch to avoid spending bandwidth on a navigation
    // that won't happen.
    val entryModifier = if (selectionMark != null) modifier else modifier.prefetchBookDetailOnPress(book.id)

    val authorName = book.authors.map { it.name }.firstOrNull().orEmpty()

    val currentEdition = book.currentEdition

    // The wavy shelf-progress signature (redesign brief): a thin sine wave under every in-progress
    // cover / beside every in-progress list row, independent of whether a deadline is tracked.
    val progressFraction = if (book.status == BookStatus.Reading) {
        book.userBookRead?.progress?.div(100f)
    } else {
        null
    }

    when (layout) {
        LibraryGridLayout.GRID_TWO_COLUMNS,
        LibraryGridLayout.GRID_THREE_COLUMNS,
            -> {
            CoverGridOverlay(dragHandle = dragHandle) {
                GridBookCell(
                    modifier = entryModifier,
                    title = book.title,
                    authorName = authorName,
                    progressFraction = progressFraction,
                    onClick = onClick,
                    onLongClick = onLongClick,
                ) { coverModifier ->
                    SelectableCover(
                        modifier = coverModifier,
                        selectionMark = selectionMark,
                    ) {
                        CoverOverlay(model = deadlineCountdownOverlay) {
                            Cover(
                                model = cover,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }
            }
        }

        LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
        LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY,
            -> {
            CoverGridOverlay(dragHandle = dragHandle) {
                CoverOnlyCell(
                    modifier = entryModifier,
                    progressFraction = progressFraction,
                    onClick = onClick,
                    onLongClick = onLongClick,
                ) { coverModifier ->
                    SelectableCover(
                        modifier = coverModifier,
                        selectionMark = selectionMark,
                    ) {
                        CoverOverlay(model = deadlineCountdownOverlay) {
                            Cover(
                                model = cover,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }
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

package nl.rhaydus.softcover.feature.library.presentation.screen.section

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.conditional
import nl.rhaydus.designsystem.modifier.grayscale
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.badge.DeadlineSummaryUiModel
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.DeadlineProgress
import nl.rhaydus.softcover.core.domain.model.DeadlineStatus
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.presentation.prefetch.prefetchBookDetailOnPress

@Composable
internal fun LayoutBookEntry(
    book: Book,
    cover: CoverUiModel?,
    layout: LibraryGridLayout,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    deadlineProgress: DeadlineProgress? = null,
    deadlineBadge: BadgeUiModel? = null,
    deadlineSummary: DeadlineSummaryUiModel? = null,
    dragHandle: (@Composable () -> Unit)? = null,
) {
    // Prefetch only makes sense when the tap opens book detail. In selection mode the tap
    // toggles selection, so skip the prefetch to avoid spending bandwidth on a navigation
    // that won't happen.
    val entryModifier = if (isSelectionMode) modifier else modifier.prefetchBookDetailOnPress(book.id)

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
                        isSelectionMode = isSelectionMode,
                        isSelected = isSelected,
                    ) {
                        LibraryGridCover(deadlineProgress = deadlineProgress) { innerModifier ->
                            Cover(
                               model = cover,
                                modifier = innerModifier,
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
                        isSelectionMode = isSelectionMode,
                        isSelected = isSelected,
                    ) {
                        LibraryGridCover(deadlineProgress = deadlineProgress) { innerModifier ->
                            Cover(
                               model = cover,
                                modifier = innerModifier,
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
                isSelectionMode = isSelectionMode,
                isSelected = isSelected,
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
                    isSelectionMode = isSelectionMode,
                    isSelected = isSelected,
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

/**
 * Wraps a book cover with the redesign's deadline **countdown** badge (top-start, "N days" + clock,
 * primary — only while [deadlineProgress] is on track or behind; the countdown itself has no meaning
 * once a deadline has expired, so [DeadlineStatus.Expired] instead reads "Expired" and the cover
 * desaturates via `Modifier.grayscale()`). This supersedes the shared `CoverOverlay`'s
 * top-end status-label badge for Library's grid/cover-only cells specifically — that shared component
 * (and its status-label badge) is unchanged and still used as-is by Reading and Book detail; see
 * `docs/reference/design-system/components.md`'s Deadline badge entry for why the two coexist. Composed *inside*
 * `SelectableCover`'s content slot (not around it) so the badge dims with the rest of the cover while
 * unselected, matching the redesign spec's selection-mode behaviour.
 */
@Composable
private fun LibraryGridCover(
    deadlineProgress: DeadlineProgress?,
    modifier: Modifier = Modifier,
    cover: @Composable (Modifier) -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        cover(
            Modifier
                .fillMaxSize()
                .conditional(
                    condition = deadlineProgress?.status == DeadlineStatus.Expired,
                    ifTrue = { Modifier.grayscale() },
                ),
        )

        if (deadlineProgress != null) {
            LibraryDeadlineCountdownBadge(
                progress = deadlineProgress,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
            )
        }
    }
}

@Composable
private fun LibraryDeadlineCountdownBadge(
    progress: DeadlineProgress,
    modifier: Modifier = Modifier,
) {
    val (container, content) = when (progress.status) {
        // Kept as errorContainer/onErrorContainer rather than the spec's flat primary for Behind — the
        // app already uses error for "needs attention" everywhere else, and losing that distinction on
        // the one badge that most needs to stand out felt like a regression (design-system.md notes it).
        DeadlineStatus.OnTrack -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        DeadlineStatus.Behind -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        DeadlineStatus.Expired -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    val label = if (progress.status == DeadlineStatus.Expired) {
        "Expired"
    } else {
        val days = progress.daysRemaining
        "$days ${if (days == 1L) "day" else "days"}"
    }

    Surface(
        modifier = modifier,
        color = container,
        contentColor = content,
        shape = RoundedCornerShape(6.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val clockIcon = drawableIconResource(
                icon = SoftcoverIcon.DateRange,
                contentDescription = "",
            )

            Icon(
                painter = clockIcon.getIconPainter(),
                contentDescription = clockIcon.contentDescription,
                modifier = Modifier.size(11.dp),
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

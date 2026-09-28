package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.softcover.core.component.badge.CoverOverlay
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.MonogramCoverForeground
import nl.rhaydus.softcover.core.designsystem.presentation.theme.MonogramCoverInk
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.prefetch.prefetchBookDetailOnPress

/**
 * A released "up next in your series" cell: cover with a corner overflow menu (opens the dismiss
 * sheet), series eyebrow, 2-line title, and the book's position in the series.
 */
@Composable
internal fun SeriesCard(
    book: Book,
    cover: CoverUiModel?,
    onClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .prefetchBookDetailOnPress(book.id)
            .pointerHandCursor()
            .pressScaleClickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Cover(
               model = cover,
                modifier = Modifier.fillMaxWidth(),
            )

            SeriesCardOverflowButton(
                onClick = onMenuClick,
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }

        book.bookSeries?.let { series ->
            Text(
                text = series.name,
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Text(
            text = book.title,
            style = MaterialTheme.editorialTypography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            text = book.positionInSeriesDisplay?.let { "Book #$it" }.orEmpty(),
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            minLines = 1,
            maxLines = 1,
        )
    }
}

/**
 * The lead card of "Up next in your series" when the next book hasn't released yet (explore-3a §4
 * "Unreleased card"): the same [Cover] every other card uses — real art when the edition has
 * it, the shared monogram fallback when it doesn't (explore-3a feedback item 1: an unreleased book is
 * never keyed off release status for its cover, only off whether art actually resolves) — plus a dated
 * [CoverOverlay] badge. Otherwise it renders exactly like a released [SeriesCard] — same overflow
 * affordance, series eyebrow, title, and "Book #N" position subline (explore-3a feedback: the
 * "Pre-order" prefix carried no information the series number didn't); only the dated badge differs.
 */
@Composable
internal fun UnreleasedSeriesCard(
    book: Book,
    cover: CoverUiModel?,
    unreleasedCoverOverlay: CoverOverlayUiModel?,
    onClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .prefetchBookDetailOnPress(book.id)
            .pointerHandCursor()
            .pressScaleClickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            CoverOverlay(model = unreleasedCoverOverlay) {
                Cover(
                    model = cover,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            SeriesCardOverflowButton(
                onClick = onMenuClick,
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }

        book.bookSeries?.let { series ->
            Text(
                text = series.name,
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Text(
            text = book.title,
            style = MaterialTheme.editorialTypography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            text = book.positionInSeriesDisplay?.let { "Book #$it" }.orEmpty(),
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            minLines = 1,
            maxLines = 1,
        )
    }
}

@Composable
private fun SeriesCardOverflowButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .padding(6.dp)
            .size(26.dp)
            .pointerHandCursor()
            .background(
                color = MonogramCoverInk.copy(alpha = 0.42f),
                shape = CircleShape,
            ),
    ) {
        val moreVertIcon = drawableIconResource(
            icon = SoftcoverIcon.MoreVert,
            contentDescription = "More options",
        )

        Icon(
            painter = moreVertIcon.getIconPainter(),
            contentDescription = moreVertIcon.contentDescription,
            tint = MonogramCoverForeground,
            modifier = Modifier.size(16.dp),
        )
    }
}

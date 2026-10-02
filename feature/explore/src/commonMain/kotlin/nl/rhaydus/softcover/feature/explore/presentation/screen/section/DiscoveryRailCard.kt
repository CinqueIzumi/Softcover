package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.common.formatDecimalNumber
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.softcover.core.component.badge.CoverOverlay
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.RatingGold
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.prefetch.prefetchBookDetailOnPress

/**
 * The shared cover-and-title shell for a discovery rail cell (explore-3a §4 "Rail cards"): cover (with
 * optional unreleased badge) and a 2-line title. [subline] supplies the caller-specific bottom row —
 * a rating for Trending, an author name for Because-you-read.
 */
@Composable
private fun DiscoveryRailCard(
    book: Book,
    cover: CoverUiModel?,
    unreleasedCoverOverlay: CoverOverlayUiModel?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subline: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .prefetchBookDetailOnPress(book.id)
            .pointerHandCursor()
            .pressScaleClickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CoverOverlay(model = unreleasedCoverOverlay) {
            Cover(
                model = cover,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        SoftcoverText(
            text = book.title,
            style = MaterialTheme.editorialTypography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(16.dp),
        ) {
            subline()
        }
    }
}

/** Trending rail cell — subline is a `★` rating (explore-3a §4 "Rail cards"). */
@Composable
internal fun TrendingCard(
    book: Book,
    cover: CoverUiModel?,
    unreleasedCoverOverlay: CoverOverlayUiModel?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DiscoveryRailCard(
        book = book,
        cover = cover,
        unreleasedCoverOverlay = unreleasedCoverOverlay,
        onClick = onClick,
        modifier = modifier,
    ) {
        if (book.rating != 0.0) {
            val starIcon = drawableIconResource(
                icon = SoftcoverIcon.StarFilled,
                contentDescription = "",
            )

            Icon(
                painter = starIcon.getIconPainter(),
                contentDescription = starIcon.contentDescription,
                tint = RatingGold,
                modifier = Modifier.size(14.dp),
            )

            Spacer(modifier = Modifier.width(4.dp))

            SoftcoverText(
                text = formatDecimalNumber(
                    book.rating,
                    fractionDigits = 1,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** "Because you read {genre}" rail cell — subline is the author name (explore-3a §4 "Rail cards"). */
@Composable
internal fun BecauseYouReadCard(
    book: Book,
    cover: CoverUiModel?,
    unreleasedCoverOverlay: CoverOverlayUiModel?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DiscoveryRailCard(
        book = book,
        cover = cover,
        unreleasedCoverOverlay = unreleasedCoverOverlay,
        onClick = onClick,
        modifier = modifier,
    ) {
        SoftcoverText(
            text = book.authorString,
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

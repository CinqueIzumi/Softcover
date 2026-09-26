package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.common.formatDecimalNumber
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.designsystem.modifier.shimmer
import nl.rhaydus.softcover.core.component.badge.Badge
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
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
    unreleasedBadge: BadgeUiModel?,
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
        Box(modifier = Modifier.fillMaxWidth()) {
            Cover(
               model = cover,
                modifier = Modifier.fillMaxWidth(),
            )

            if (unreleasedBadge != null) {
                Badge(
                    model = unreleasedBadge,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(all = 6.dp),
                )
            }
        }

        Text(
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

/**
 * Mirrors [DiscoveryRailCard]'s own anatomy: the cover, the 2-line (`minLines = 2`) title, and the
 * fixed 16dp-tall subline row. The title reserves two 20dp bars rather than one 16dp bar: the
 * real `titleMedium` (M3's default 16sp/24dp) two-line block is 48dp tall, and splitting it across
 * two `Column` children (each carrying the outer 8dp `spacedBy` gap) means the bars only need to
 * sum to 40dp (48 minus the one extra 8dp gap this split introduces) for the reserved footprint to
 * still land on 48dp - a single 16dp bar would leave the crossfade shrinking once the real 2-line
 * title lands.
 */
@Composable
private fun RailCardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(6.dp))
                .shimmer(isLoading = true),
        )

        Box(
            modifier = Modifier
                .height(20.dp)
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(4.dp))
                .shimmer(isLoading = true),
        )

        Box(
            modifier = Modifier
                .height(20.dp)
                .fillMaxWidth(0.55f)
                .clip(RoundedCornerShape(4.dp))
                .shimmer(isLoading = true),
        )

        Box(
            modifier = Modifier
                .height(16.dp)
                .fillMaxWidth(0.4f)
                .clip(RoundedCornerShape(4.dp))
                .shimmer(isLoading = true),
        )
    }
}

/** Trending rail cell — subline is a `★` rating (explore-3a §4 "Rail cards"). */
@Composable
internal fun TrendingCard(
    book: Book,
    cover: CoverUiModel?,
    unreleasedBadge: BadgeUiModel?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DiscoveryRailCard(
        book = book,
        cover = cover,
        unreleasedBadge = unreleasedBadge,
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

            Text(
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

@Composable
internal fun TrendingCardSkeleton(modifier: Modifier = Modifier) = RailCardSkeleton(modifier = modifier)

/** "Because you read {genre}" rail cell — subline is the author name (explore-3a §4 "Rail cards"). */
@Composable
internal fun BecauseYouReadCard(
    book: Book,
    cover: CoverUiModel?,
    unreleasedBadge: BadgeUiModel?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DiscoveryRailCard(
        book = book,
        cover = cover,
        unreleasedBadge = unreleasedBadge,
        onClick = onClick,
        modifier = modifier,
    ) {
        Text(
            text = book.authorString,
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun BecauseYouReadCardSkeleton(modifier: Modifier = Modifier) = RailCardSkeleton(modifier = modifier)

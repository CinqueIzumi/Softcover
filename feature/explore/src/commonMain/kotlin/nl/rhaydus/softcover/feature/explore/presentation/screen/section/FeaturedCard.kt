package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.designsystem.modifier.shimmer
import nl.rhaydus.softcover.core.component.badge.Badge
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.domain.model.FEATURED_RELEASE_WINDOW_DAYS

/**
 * The feed-opening "upcoming release" hero (explore-3a §4 "Featured card"). Cover-beside-text, never
 * full-bleed — a 2:3 jacket crops badly in a banner. Ships with only the "Want to read" action (explore-
 * 3a deviation 1: the spec's "Remind me" pill and the release-reminder sheet are dropped — the app has no
 * future-notification scheduling infrastructure).
 *
 * The card **names itself** with the DS §2.3 inline 20×1 hairline eyebrow on its own top row, rather
 * than being introduced by a full `EditorialSectionHeader` the way every rail below it is. It briefly
 * had one: accent bar, `headline` headline and a description sentence together pushed the card most of
 * the way down the first screen — far too much chrome for a single card whose pick rotates weekly. The
 * inline register says the same thing in one line, which is exactly what the register exists for.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FeaturedCard(
    book: Book,
    // Nullable for the one frame between `featuredUpcomingRelease` landing and `CoverModelsCollector`
    // deriving its cover: the hero must keep its footprint rather than vanish and pop back in.
    cover: CoverUiModel?,
    releaseBadge: BadgeUiModel?,
    onClick: () -> Unit,
    onWantToReadClick: () -> Unit,
    onRemoveFromLibraryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val inLibrary = book.userBook != null

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .pointerHandCursor()
            .pressScaleClickable(onClick = onClick)
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .height(1.dp)
                    .width(20.dp)
                    .background(MaterialTheme.colorScheme.primary),
            )

            Spacer(modifier = Modifier.width(10.dp))

            // The eyebrow states the whole pick rule: the window comes from the same domain
            // constant the query is bounded by, and the "N readers waiting" line below supplies
            // the ranking basis, so the two rows together say "the most-shelved book out in the
            // next 30 days" without spending a sentence on it.
            Text(
                text = "Most anticipated · next $FEATURED_RELEASE_WINDOW_DAYS days".uppercase(),
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // A FlowRow rather than a Row: the date badge and the readers line together run close to the
        // card's inner width on a narrow phone, and a plain Row would clip the tail of "N readers
        // waiting" outright at the larger UI scales Appearance offers. Wrapping to a second line
        // costs nothing when there is room.
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            if (releaseBadge != null) {
                Badge(model = releaseBadge)
            }

            // Explore-3a deviation 3 kept this at a bare "readers" because usersCount is an
            // all-shelves total rather than a want-to-read count. "Waiting" is added back only
            // because this card is exclusively the *unreleased* hero: a book that isn't out yet
            // can only be on a shelf in anticipation, so the total and the wording agree here in a
            // way they would not on a released book's card.
            Text(
                text = "${formatGroupedNumber(book.usersCount)} readers waiting",
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Cover(
               model = cover,
                modifier = Modifier.width(96.dp),
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.editorialTypography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "By ${book.authorString}".uppercase(),
                    style = MaterialTheme.editorialTypography.eyebrowSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (book.headline.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = book.headline,
                        style = MaterialTheme.editorialTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        RhaydusButton(
            label = if (inLibrary) "Added to library" else "Want to read",
            style = ButtonStyle.FILLED,
            onClick = if (inLibrary) onRemoveFromLibraryClick else onWantToReadClick,
        )
    }
}

/**
 * Mirrors [FeaturedCard]'s own anatomy - the inline eyebrow row, the badge/readers row, the
 * cover-and-text row (title, author, and a headline placeholder line), and the trailing button - as
 * shimmer bars, rather than just the cover-and-title pair the card's *content* leads with. Matching
 * every row the loaded card renders (including the button) keeps the two within a few dp of the same
 * total height, so `SkeletonCrossfade` swaps between them without a visible resize.
 */
@Composable
internal fun FeaturedCardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(18.dp),
    ) {
        Box(
            modifier = Modifier
                .height(12.dp)
                .width(180.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmer(isLoading = true),
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .width(120.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .shimmer(isLoading = true),
            )

            Box(
                modifier = Modifier
                    .height(14.dp)
                    .width(90.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer(isLoading = true),
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer(isLoading = true),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer(isLoading = true),
                )

                Box(
                    modifier = Modifier
                        .height(12.dp)
                        .fillMaxWidth(0.6f)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer(isLoading = true),
                )

                Box(
                    modifier = Modifier
                        .height(12.dp)
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer(isLoading = true),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .height(ButtonSize.S.height)
                .fillMaxWidth()
                .clip(RoundedCornerShape(percent = 50))
                .shimmer(isLoading = true),
        )
    }
}

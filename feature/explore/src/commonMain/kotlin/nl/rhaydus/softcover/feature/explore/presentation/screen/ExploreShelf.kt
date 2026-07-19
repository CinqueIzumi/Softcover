package nl.rhaydus.softcover.feature.explore.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.time.Duration.Companion.seconds
import nl.rhaydus.common.formatDecimalNumber
import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.designsystem.component.AdaptiveModalSheet
import nl.rhaydus.designsystem.component.LocalModalSheetDismiss
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.noRippleClickable
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.designsystem.modifier.shimmer
import nl.rhaydus.designsystem.util.SkeletonCrossfade
import nl.rhaydus.softcover.core.designsystem.presentation.component.EditionImage
import nl.rhaydus.softcover.core.designsystem.presentation.component.UnreleasedBadge
import nl.rhaydus.softcover.core.designsystem.presentation.component.formatCompactRelease
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.prefetch.prefetchBookDetailOnPress
import nl.rhaydus.softcover.core.designsystem.presentation.theme.MonogramCoverForeground
import nl.rhaydus.softcover.core.designsystem.presentation.theme.MonogramCoverInk
import nl.rhaydus.softcover.core.designsystem.presentation.theme.MoodInkCosyBackground
import nl.rhaydus.softcover.core.designsystem.presentation.theme.MoodInkCosyForeground
import nl.rhaydus.softcover.core.designsystem.presentation.theme.MoodInkDreadBackground
import nl.rhaydus.softcover.core.designsystem.presentation.theme.MoodInkDreadEyebrow
import nl.rhaydus.softcover.core.designsystem.presentation.theme.MoodInkDreadForeground
import nl.rhaydus.softcover.core.designsystem.presentation.theme.MoodInkHeartWrenchBackground
import nl.rhaydus.softcover.core.designsystem.presentation.theme.MoodInkHeartWrenchForeground
import nl.rhaydus.softcover.core.designsystem.presentation.theme.RatingGold
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.transition.bookCoverTransitionKey
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook
import nl.rhaydus.softcover.feature.explore.domain.model.ExploreSortMode
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnAddBookToLibraryClickAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnBecauseYouReadGenreSelectedAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnDismissContinueSeriesAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnDismissContinueSeriesBookAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnMoodChipClickAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnQueryChangeAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRemoveAllSearchQueriesClickedAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRemoveBookFromLibraryClickAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRemoveSearchQueryClickedAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnSortModeChangeAction

internal const val TRENDING_SKELETON_COUNT = 4
internal const val CONTINUE_SERIES_SKELETON_COUNT = 4
internal const val BECAUSE_YOU_READ_SKELETON_COUNT = 4
internal const val MOOD_SKELETON_COUNT = 4

// How many items from the end of the search-results list/grid trigger the next page (explore-3a
// feedback item 7) — shared between the mobile LazyColumn and the desktop LazyVerticalGrid so the
// two platforms feel the same distance from the edge before they fetch.
internal const val SEARCH_RESULTS_LOAD_MORE_THRESHOLD = 4

// Distinct shared-element surfaces so the same book showing up in more than one rail
// registers a unique key in the SharedTransitionScope.
internal const val SURFACE_TRENDING = "explore-trending"
internal const val SURFACE_UP_NEXT = "explore-up-next"
internal const val SURFACE_BECAUSE_YOU_READ = "explore-because-you-read"
internal const val SURFACE_FEATURED = "explore-featured"

private val MOOD_TILE_MIN_HEIGHT = 104.dp

// Mood-tile title exception (design-system.md §2.2): the spec's own literal type role for this
// title ("Mood tile title · italic 600 · 19/22") sits between headlineSmall and titleLarge in the
// editorial scale, so headlineSmall is sized down to it rather than left at its full 24sp.
private val MOOD_TILE_TITLE_FONT_SIZE = 19.sp
private val MOOD_TILE_TITLE_LINE_HEIGHT = 22.sp
// region Section header skeleton
/**
 * Mirrors [EditorialSectionHeader]'s own anatomy (accent bar, eyebrow row, headline) as shimmer
 * bars. Only a section whose headline text itself depends on data not yet resolved - Because-you-
 * read's genre - needs this: every other section header here is a static string, always known.
 * Reused by both the mobile and desktop Because-you-read layouts.
 */
@Composable
internal fun EditorialSectionHeaderSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .width(32.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .shimmer(isLoading = true),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .height(12.dp)
                    .width(100.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer(isLoading = true),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .height(22.dp)
                .fillMaxWidth(0.5f)
                .clip(RoundedCornerShape(4.dp))
                .shimmer(isLoading = true),
        )
    }
}
// endregion
// region Featured card
/**
 * The feed-opening "upcoming release" hero (explore-3a §4 "Featured card"). Cover-beside-text, never
 * full-bleed — a 2:3 jacket crops badly in a banner. Ships with only the "Want to read" action (explore-
 * 3a deviation 1: the spec's "Remind me" pill and the release-reminder sheet are dropped — the app has no
 * future-notification scheduling infrastructure).
 */
@Composable
internal fun FeaturedCard(
    book: Book,
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
        val releaseDate = book.effectiveReleaseDate

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (releaseDate != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text(
                        text = "Arriving ${releaseDate.formatCompactRelease()}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }

            // "Readers" rather than "readers waiting" (explore-3a deviation 3): usersCount is an
            // all-shelves total, not a want-to-read count, so the copy stays defensible.
            Text(
                text = "${formatGroupedNumber(book.usersCount)} readers",
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.Top,
        ) {
            EditionImage(
                edition = book.currentEdition,
                defaultEdition = book.defaultEdition,
                isLoading = false,
                fallbackCoverUrl = book.coverUrl,
                coverlessTitle = book.title,
                modifier = Modifier.width(96.dp),
                elevation = 6.dp,
                cornerRadius = 4.dp,
                sharedTransitionKey = bookCoverTransitionKey(
                    editionId = book.currentEdition?.id,
                    bookId = book.id,
                    surface = SURFACE_FEATURED,
                ),
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
 * Mirrors [FeaturedCard]'s own anatomy - the badge/readers row, the cover-and-text row (title,
 * author, and a headline placeholder line), and the trailing button - as shimmer bars, rather than
 * just the cover-and-title pair the card's *content* leads with. Matching every row the loaded
 * card renders (including the button) keeps the two within a few dp of the same total height, so
 * [SkeletonCrossfade] swaps between them without a visible resize.
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
// endregion
// region Discovery rail cards (Trending / Because you read)
/**
 * The shared cover-and-title shell for a discovery rail cell (explore-3a §4 "Rail cards"): cover (with
 * optional unreleased badge) and a 2-line title. [subline] supplies the caller-specific bottom row —
 * a rating for Trending, an author name for Because-you-read.
 */
@Composable
private fun DiscoveryRailCard(
    book: Book,
    onClick: () -> Unit,
    surface: String,
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
            EditionImage(
                edition = book.currentEdition,
                defaultEdition = book.defaultEdition,
                isLoading = false,
                fallbackCoverUrl = book.coverUrl,
                coverlessTitle = book.title,
                modifier = Modifier.fillMaxWidth(),
                elevation = 6.dp,
                cornerRadius = 4.dp,
                sharedTransitionKey = bookCoverTransitionKey(
                    editionId = book.currentEdition?.id,
                    bookId = book.id,
                    surface = surface,
                ),
            )

            if (book.isUnreleased) {
                book.effectiveReleaseDate?.let { date ->
                    UnreleasedBadge(
                        releaseDate = date,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(all = 6.dp),
                    )
                }
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DiscoveryRailCard(
        book = book,
        onClick = onClick,
        surface = SURFACE_TRENDING,
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DiscoveryRailCard(
        book = book,
        onClick = onClick,
        surface = SURFACE_BECAUSE_YOU_READ,
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
// endregion
// region Up-next-in-series cards
/**
 * A released "up next in your series" cell: cover with a corner overflow menu (opens the dismiss
 * sheet), series eyebrow, 2-line title, and the book's position in the series.
 */
@Composable
internal fun SeriesCard(
    book: Book,
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
            EditionImage(
                edition = book.currentEdition,
                defaultEdition = book.defaultEdition,
                isLoading = false,
                fallbackCoverUrl = book.coverUrl,
                coverlessTitle = book.title,
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp,
                cornerRadius = 4.dp,
                sharedTransitionKey = bookCoverTransitionKey(
                    editionId = book.currentEdition?.id,
                    bookId = book.id,
                    surface = SURFACE_UP_NEXT,
                ),
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
 * Mirrors [SeriesCard] / [UnreleasedSeriesCard]'s own anatomy: cover, series eyebrow
 * (`eyebrowSmall`, M3's default 16dp line height), the 2-line (`minLines = 2`) `titleSmall` title
 * as two bars, and the "Book #N" `bodySmall` line. Every bar here is 16dp: the real title's
 * 2×20dp lines total 40dp, and splitting that across two `Column` children (each carrying the
 * outer 8dp `spacedBy` gap) only needs 2×16dp (32dp, plus the one extra 8dp gap the split
 * introduces) to still land on the same 40dp footprint - so, unlike a rail card's `titleMedium`
 * (§ [RailCardSkeleton]), the smaller `titleSmall` line height happens to converge on the same
 * 16dp bar height every other line here already uses.
 */
@Composable
internal fun SeriesCardSkeleton(modifier: Modifier = Modifier) {
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
                .height(16.dp)
                .fillMaxWidth(0.7f)
                .clip(RoundedCornerShape(4.dp))
                .shimmer(isLoading = true),
        )

        Box(
            modifier = Modifier
                .height(16.dp)
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(4.dp))
                .shimmer(isLoading = true),
        )

        Box(
            modifier = Modifier
                .height(16.dp)
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

/**
 * The lead card of "Up next in your series" when the next book hasn't released yet (explore-3a §4
 * "Unreleased card"): the same [EditionImage] every other card uses — real art when the edition has
 * it, the shared monogram fallback when it doesn't (explore-3a feedback item 1: an unreleased book is
 * never keyed off release status for its cover, only off whether art actually resolves) — plus a dated
 * [UnreleasedBadge]. Otherwise it renders exactly like a released [SeriesCard] — same overflow
 * affordance, series eyebrow, title, and "Book #N" position subline (explore-3a feedback: the
 * "Pre-order" prefix carried no information the series number didn't); only the dated badge differs.
 */
@Composable
internal fun UnreleasedSeriesCard(
    book: Book,
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
            EditionImage(
                edition = book.currentEdition,
                defaultEdition = book.defaultEdition,
                isLoading = false,
                fallbackCoverUrl = book.coverUrl,
                coverlessTitle = book.title,
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp,
                cornerRadius = 4.dp,
                sharedTransitionKey = bookCoverTransitionKey(
                    editionId = book.currentEdition?.id,
                    bookId = book.id,
                    surface = SURFACE_UP_NEXT,
                ),
            )

            // The dated badge (explore-3a feedback item 8: names the release date, e.g. "Out Sep 2" —
            // never the generic "Coming soon" a discovery rail's ordinary UnreleasedBadge doesn't carry
            // either), same top-start placement and Compact style every other unreleased cover uses.
            book.effectiveReleaseDate?.let { date ->
                UnreleasedBadge(
                    releaseDate = date,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(all = 6.dp),
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
// endregion
// region Up-next dismiss sheet
/**
 * The shared dismiss-options sheet host for an "up next" series book. Wires the two dismiss actions
 * through [runAction] so both the mobile carousel and the desktop grid open an identical sheet, and
 * closes through [LocalModalSheetDismiss] so each form animates out the way it should. [onDismiss]
 * clears the host's selected book once the sheet has closed.
 */
@Composable
internal fun ContinueSeriesMenuSheet(
    book: Book,
    runAction: (ExploreAction) -> Unit,
    onDismiss: () -> Unit,
) {
    AdaptiveModalSheet(onDismissRequest = onDismiss) {
        val dismiss = LocalModalSheetDismiss.current

        ContinueSeriesDismissSheet(
            book = book,
            onDismissBookClick = {
                runAction(
                    OnDismissContinueSeriesBookAction(
                        book = DismissedSeriesBook(
                            bookId = book.id,
                            title = book.title,
                            coverUrl = book.coverUrl,
                            authorText = book.authorString.takeIf { it.isNotBlank() },
                            seriesName = book.bookSeries?.name,
                            seriesId = book.bookSeries?.id,
                            // The series cursor moves past this book's *last* position, so an omnibus
                            // spanning several positions can't re-match on the next fetch.
                            seriesPosition = book.positionsInSeries.lastOrNull(),
                        ),
                    ),
                )

                dismiss()
            },
            onDismissSeriesClick = {
                val series = book.bookSeries ?: return@ContinueSeriesDismissSheet

                runAction(
                    OnDismissContinueSeriesAction(
                        seriesId = series.id,
                        seriesName = series.name,
                        coverUrl = book.coverUrl,
                        authorText = book.authorString.takeIf { it.isNotBlank() },
                        bookCount = series.amountOfBooks,
                    ),
                )

                dismiss()
            },
            onCancelClick = dismiss,
        )
    }
}

@Composable
private fun ContinueSeriesDismissSheet(
    book: Book,
    onDismissBookClick: () -> Unit,
    onDismissSeriesClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(state = rememberScrollState())
            .imePadding()
            .padding(bottom = 24.dp),
    ) {
        EditorialSectionHeader(
            eyebrow = book.bookSeries?.name ?: "Up next",
            headline = book.title,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        DismissSheetOption(
            icon = SoftcoverIcon.VisibilityOff,
            label = "Hide this book",
            description = "Just this title stops showing up here",
            onClick = onDismissBookClick,
        )

        book.bookSeries?.let { series ->
            DismissSheetOption(
                icon = SoftcoverIcon.LibraryBooks,
                label = "Hide everything from ${series.name}",
                description = "Hide every book in this series from Up next",
                onClick = onDismissSeriesClick,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Cancel",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .pointerHandCursor()
                .noRippleClickable(onClick = onCancelClick)
                .padding(vertical = 12.dp),
        )
    }
}

@Composable
private fun DismissSheetOption(
    icon: SoftcoverIcon,
    label: String,
    description: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pointerHandCursor()
            .clickable(onClick = onClick)
            .padding(
                horizontal = 14.dp,
                vertical = 12.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        val iconResource = drawableIconResource(
            icon = icon,
            contentDescription = "",
        )

        Icon(
            painter = iconResource.getIconPainter(),
            contentDescription = iconResource.contentDescription,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp),
        )

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = description,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
// endregion
// region Browse by mood
/**
 * The 2-column "Browse by mood" grid (explore-3a §4 "Mood grid"). A tap always runs a mood-filtered
 * search — a mood never earns its own feed rail (see [ExploreScreenUiState.searchPhase]'s modelling
 * note). Tile ink cycles by position through the four fixed looks the spec defines; a fifth tag onward
 * repeats the cycle rather than introducing a new colour. Each row is measured at
 * [androidx.compose.foundation.layout.IntrinsicSize.Max] so two tiles whose titles wrap to a
 * different number of lines still land at the same height (explore-3a feedback item 3) — otherwise
 * a two-line mood label next to a one-line one would leave the row visibly uneven.
 */
@Composable
internal fun MoodGrid(
    moods: List<MoodTag>,
    isLoading: Boolean,
    runAction: (ExploreAction) -> Unit,
) {
    if (isLoading.not() && moods.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        EditorialSectionHeader(
            eyebrow = "Browse by mood",
            headline = "How do you want to feel?",
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        SkeletonCrossfade(
            isLoading = isLoading,
            modifier = Modifier.padding(horizontal = 24.dp),
            label = "MoodGrid",
        ) { loading ->
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (loading) {
                    repeat(MOOD_SKELETON_COUNT / 2) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MoodTileSkeleton(modifier = Modifier.weight(1f))

                            MoodTileSkeleton(modifier = Modifier.weight(1f))
                        }
                    }
                } else {
                    moods.chunked(2).forEachIndexed { rowIndex, row ->
                        Row(
                            modifier = Modifier.height(IntrinsicSize.Max),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            row.forEachIndexed { columnIndex, mood ->
                                MoodTile(
                                    mood = mood,
                                    index = rowIndex * 2 + columnIndex,
                                    onClick = { runAction(OnMoodChipClickAction(mood = mood)) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                )
                            }

                            if (row.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoodTile(
    mood: MoodTag,
    index: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background: Color
    val foreground: Color
    val eyebrowColor: Color
    val sublineColor: Color

    when (index % 4) {
        0 -> {
            background = MoodInkCosyBackground
            foreground = MoodInkCosyForeground
            eyebrowColor = MoodInkCosyForeground
            sublineColor = MoodInkCosyForeground.copy(alpha = 0.65f)
        }

        1 -> {
            background = MoodInkDreadBackground
            foreground = MoodInkDreadForeground
            eyebrowColor = MoodInkDreadEyebrow
            sublineColor = MoodInkDreadForeground.copy(alpha = 0.6f)
        }

        3 -> {
            background = MoodInkHeartWrenchBackground
            foreground = MoodInkHeartWrenchForeground
            eyebrowColor = MoodInkHeartWrenchForeground
            sublineColor = MoodInkHeartWrenchForeground.copy(alpha = 0.6f)
        }

        else -> {
            background = MaterialTheme.colorScheme.surfaceContainerHigh
            foreground = MaterialTheme.colorScheme.onSurface
            eyebrowColor = MaterialTheme.colorScheme.primary
            sublineColor = MaterialTheme.colorScheme.onSurfaceVariant
        }
    }

    Column(
        modifier = modifier
            .heightIn(min = MOOD_TILE_MIN_HEIGHT)
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .pointerHandCursor()
            .pressScaleClickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = "MOOD",
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = eyebrowColor,
            )

            val searchIcon = drawableIconResource(
                icon = SoftcoverIcon.Search,
                contentDescription = "",
            )

            Icon(
                painter = searchIcon.getIconPainter(),
                contentDescription = searchIcon.contentDescription,
                tint = foreground.copy(alpha = 0.75f),
                modifier = Modifier.size(16.dp),
            )
        }

        Column {
            // Mood-tile title exception (design-system.md §2.2): headlineSmall sized down to the
            // spec's own literal 19/22 mood-tile-title role rather than the full headlineMedium the
            // grid shipped with, which read oversize against a ~140dp half-grid tile and forced a
            // mid-word break on a long single-word mood ("Adventurous", "Mysterious") — the API
            // returns these lowercase and unbroken by spaces, so there is no word boundary to wrap
            // on until the glyphs themselves shrink to fit. `Hyphens.None` + `LineBreak.Heading`
            // mirror the same mid-word-break guard `CoverlessTitleCover` uses.
            Text(
                text = mood.label.toTitleCaseWords(),
                style = MaterialTheme.editorialTypography.headlineSmall.copy(
                    fontSize = MOOD_TILE_TITLE_FONT_SIZE,
                    lineHeight = MOOD_TILE_TITLE_LINE_HEIGHT,
                    hyphens = Hyphens.None,
                    lineBreak = LineBreak.Heading,
                ),
                color = foreground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = "${formatGroupedNumber(mood.bookCount)} books",
                style = MaterialTheme.editorialTypography.bodySmall,
                color = sublineColor,
            )
        }
    }
}

@Composable
private fun MoodTileSkeleton(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .heightIn(min = MOOD_TILE_MIN_HEIGHT)
            .clip(RoundedCornerShape(16.dp))
            .shimmer(isLoading = true),
    )
}

/**
 * Presentation-only title-casing for a mood label (explore-3a feedback item 3): the API returns
 * moods lowercase ("adventurous", "cosy & comforting"). The domain model stays untouched — only the
 * render layer capitalizes each whitespace-separated word before it reaches a `Text`.
 */
private fun String.toTitleCaseWords(): String =
    split(' ').joinToString(" ") { word -> word.replaceFirstChar { it.titlecase() } }
// endregion
// region Recent searches
/**
 * The feed's recent-search history block (explore-3a §4 "Recent searches"): an eyebrow + "Clear all"
 * row over a wrapping row of tap-to-search pill chips. No per-chip remove here — that affordance lives
 * on the search-focus state's recent rows ([SearchFocusRecentRow]) instead.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun RecentSearchesSection(
    queries: List<String>,
    runAction: (ExploreAction) -> Unit,
) {
    if (queries.isEmpty()) return

    Column(
        modifier = Modifier.padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "RECENT SEARCHES",
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            RhaydusButton(
                label = "Clear all",
                onClick = { runAction(OnRemoveAllSearchQueriesClickedAction()) },
                style = ButtonStyle.TEXT,
            )
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            queries.forEach { query ->
                RecentSearchChip(
                    query = query,
                    onClick = {
                        runAction(
                            OnQueryChangeAction(
                                newQuery = query,
                                searchDelay = 0.seconds,
                            ),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun RecentSearchChip(
    query: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.pointerHandCursor(),
        shape = RoundedCornerShape(percent = 50),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Text(
            text = query,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 9.dp),
        )
    }
}
// endregion
// region Search focus state
/**
 * The search-focus overlay content (explore-3a §4 "Search focus state"): the recent-query history as
 * full rows (clock leading icon, trailing per-row remove) followed by "Try a mood" chips. Replaces the
 * feed while the field is focused and empty — no editorial recommendations here, those live on the feed.
 */
@Composable
internal fun SearchFocusContent(
    queries: List<String>,
    moods: List<MoodTag>,
    runAction: (ExploreAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 20.dp)) {
        if (queries.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "RECENT",
                    style = MaterialTheme.editorialTypography.eyebrowSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                RhaydusButton(
                    label = "Clear all",
                    onClick = { runAction(OnRemoveAllSearchQueriesClickedAction()) },
                    style = ButtonStyle.TEXT,
                )
            }

            Column(modifier = Modifier.padding(top = 8.dp)) {
                queries.forEach { query ->
                    SearchFocusRecentRow(
                        query = query,
                        onClick = {
                            runAction(
                                OnQueryChangeAction(
                                    newQuery = query,
                                    searchDelay = 0.seconds,
                                ),
                            )
                        },
                        onRemoveClick = { runAction(OnRemoveSearchQueryClickedAction(query = query)) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        if (moods.isNotEmpty()) {
            Text(
                text = "TRY A MOOD",
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            FlowRowMoodChips(
                moods = moods,
                onMoodClick = { mood -> runAction(OnMoodChipClickAction(mood = mood)) },
            )
        }
    }
}

@Composable
private fun SearchFocusRecentRow(
    query: String,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pointerHandCursor()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            val clockIcon = drawableIconResource(
                icon = SoftcoverIcon.History,
                contentDescription = "",
            )

            Icon(
                painter = clockIcon.getIconPainter(),
                contentDescription = clockIcon.contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )

            Text(
                text = query,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            val closeIcon = drawableIconResource(
                icon = SoftcoverIcon.Close,
                contentDescription = "Remove query",
            )

            Icon(
                painter = closeIcon.getIconPainter(),
                contentDescription = closeIcon.contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(15.dp)
                    .noRippleClickable(onClick = onRemoveClick),
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowMoodChips(
    moods: List<MoodTag>,
    onMoodClick: (MoodTag) -> Unit,
) {
    FlowRow(
        modifier = Modifier.padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        moods.forEach { mood ->
            RecentSearchChip(
                query = mood.label.toTitleCaseWords(),
                onClick = { onMoodClick(mood) },
            )
        }
    }
}
// endregion
// region Search results
/** The sort control for search results (explore-3a deviation 4: Relevance / Popularity only). */
@Composable
internal fun SortChip(
    sortMode: ExploreSortMode,
    runAction: (ExploreAction) -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            onClick = { menuExpanded = true },
            modifier = Modifier.pointerHandCursor(),
            shape = RoundedCornerShape(percent = 50),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ) {
            Text(
                text = "Sort · ${sortMode.displayLabel}",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            ExploreSortMode.entries.forEach { mode ->
                DropdownMenuItem(
                    text = { Text(text = mode.displayLabel) },
                    onClick = {
                        menuExpanded = false
                        runAction(OnSortModeChangeAction(mode = mode))
                    },
                )
            }
        }
    }
}

private val ExploreSortMode.displayLabel: String
    get() = when (this) {
        ExploreSortMode.RELEVANCE -> "Relevance"
        ExploreSortMode.POPULARITY -> "Popularity"
    }

/** The "SHOWING {N} RESULTS" header (explore-3a §4 "Search results"). */
@Composable
internal fun SearchResultsHeader(
    resultCount: Int,
    subtitle: String?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .height(4.dp)
                .width(32.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary),
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Showing $resultCount results".uppercase(),
            style = MaterialTheme.editorialTypography.eyebrow,
            color = MaterialTheme.colorScheme.primary,
        )

        if (subtitle != null) {
            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * A search-result row: cover, category eyebrow + title + byline + metadata, and the add/remove-from-
 * library bookmark toggle. Shared between the mobile single-column list and the desktop multi-column
 * results grid. Hover/cursor are baked in and inert on touch.
 */
@Composable
internal fun SearchResultRow(
    book: Book,
    onBookClick: (Book, String?) -> Unit,
    runAction: (ExploreAction) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .prefetchBookDetailOnPress(book.id)
            .pointerHandCursor()
            .pressScaleClickable(
                onClick = {
                    onBookClick(
                        book,
                        null,
                    )
                },
            )
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        EditionImage(
            edition = book.currentEdition,
            modifier = Modifier.width(54.dp),
            isLoading = false,
            defaultEdition = book.defaultEdition,
            fallbackCoverUrl = book.coverUrl,
            coverlessTitle = book.title,
            cornerRadius = 4.dp,
            sharedTransitionKey = bookCoverTransitionKey(
                editionId = book.currentEdition?.id,
                bookId = book.id,
            ),
        )

        Column(modifier = Modifier.weight(1f)) {
            // Series only — a book with no series shows no eyebrow here (explore-3a feedback: a
            // fallback tag like "classics"/"dark" in the series slot read as noise, not signal).
            val category = book.seriesText

            if (category != null) {
                Text(
                    text = category.uppercase(),
                    style = MaterialTheme.editorialTypography.eyebrowSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = book.title,
                style = MaterialTheme.editorialTypography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "By ${book.authorString}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                val metaParts = listOfNotNull(
                    book.releaseYear.takeIf { it != -1 }?.toString(),
                    "${formatGroupedNumber(book.usersCount)} readers",
                )

                Text(
                    text = metaParts.joinToString(separator = " · "),
                    style = MaterialTheme.editorialTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (book.rating != 0.0) {
                    Text(
                        text = " · ",
                        style = MaterialTheme.editorialTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

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
                        style = MaterialTheme.editorialTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        val addedToLibrary = book.userBook != null

        BookmarkToggle(
            checked = addedToLibrary,
            onCheckedChange = { newValue ->
                when (newValue) {
                    true -> runAction(OnAddBookToLibraryClickAction(book = book))
                    false -> runAction(OnRemoveBookFromLibraryClickAction(book = book))
                }
            },
        )
    }
}

/** The 44×44, radius-12 bookmark-to-library toggle (explore-3a §4 "Search results"). */
@Composable
private fun BookmarkToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Surface(
        onClick = { onCheckedChange(checked.not()) },
        modifier = Modifier
            .size(44.dp)
            .pointerHandCursor(),
        shape = RoundedCornerShape(12.dp),
        color = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = if (checked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            val iconResource = when {
                checked -> SoftcoverIcon.BookmarkAdded
                else -> SoftcoverIcon.BookmarkAdd
            }

            val contentDescription = when {
                checked -> "Remove from library"
                else -> "Add to library"
            }

            val bookmarkIcon = drawableIconResource(
                icon = iconResource,
                contentDescription = contentDescription,
            )

            Icon(
                painter = bookmarkIcon.getIconPainter(),
                contentDescription = bookmarkIcon.contentDescription,
            )
        }
    }
}
// endregion
// region Because-you-read genre picker
/**
 * The "because you read {genre}" picker (explore-3a feedback item 10, rebuilt a second time per
 * follow-up feedback): a **bottom sheet** mirroring Library's own
 * [nl.rhaydus.softcover.feature.library.presentation.component.LibraryShelvesSheet] — not the
 * anchored `DropdownMenu` this control used before. The anchor stays the same compact
 * `eyebrowSmall` label naming the resolved [genre] with a trailing `primary` chevron
 * (`"Genre · {genre}"`, uppercase, the same sheet-opening-label register Library's own sort
 * control uses); tapping it opens [BecauseYouReadGenreSheet]. Hidden when the reader has no genre
 * options to switch between (nothing to pick from).
 */
@Composable
internal fun BecauseYouReadGenreControl(
    genre: String,
    options: List<String>,
    runAction: (ExploreAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (options.isEmpty()) return

    var sheetVisible by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .pointerHandCursor()
            .pressScaleClickable(onClick = { sheetVisible = true }),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Genre · $genre".uppercase(),
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        // Decorative — the adjacent genre label already carries the control's meaning, the
        // same convention MoodTile's search glyph and the rating star use elsewhere in this
        // file; a real description here would double-announce the one tap target.
        val chevronIcon = drawableIconResource(
            icon = SoftcoverIcon.ArrowDropDown,
            contentDescription = "",
        )

        Icon(
            painter = chevronIcon.getIconPainter(),
            contentDescription = chevronIcon.contentDescription,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp),
        )
    }

    if (sheetVisible) {
        BecauseYouReadGenreSheet(
            genre = genre,
            options = options,
            runAction = runAction,
            onDismissRequest = { sheetVisible = false },
        )
    }
}

/**
 * The genre-picker sheet body: [EditorialSectionHeader] (accent bar + "GENRE" eyebrow + "Pick a
 * genre" headline — a short neutral category word rather than repeating the "Because you read"
 * rail name, which read as redundant and option-like once opened inside the sheet) over a vertical
 * list of hairline-divided rows, one for "Auto (most-read)"
 * (the null-genre default, checked whenever [genre] doesn't match any switchable [options] entry)
 * and one per [options] entry — laid out exactly like `LibraryShelvesSheet`'s shelf rows
 * ([BecauseYouReadGenreSheetRow] mirrors `ShelvesSheetRow`'s italic label + active check, minus the
 * trailing item count a genre has none of). Selecting a row dispatches
 * `OnBecauseYouReadGenreSelectedAction` and dismisses.
 */
@Composable
private fun BecauseYouReadGenreSheet(
    genre: String,
    options: List<String>,
    runAction: (ExploreAction) -> Unit,
    onDismissRequest: () -> Unit,
) {
    AdaptiveModalSheet(onDismissRequest = onDismissRequest) {
        val dismiss = LocalModalSheetDismiss.current

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(state = rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            EditorialSectionHeader(
                eyebrow = "Genre",
                headline = "Pick a genre",
            )

            Spacer(modifier = Modifier.height(6.dp))

            BecauseYouReadGenreSheetRow(
                label = "Auto (most-read)",
                active = options.none { it == genre },
                onClick = {
                    runAction(OnBecauseYouReadGenreSelectedAction(genre = null))

                    dismiss()
                },
            )

            options.forEach { option ->
                BecauseYouReadGenreSheetRow(
                    label = option,
                    active = option == genre,
                    onClick = {
                        runAction(OnBecauseYouReadGenreSelectedAction(genre = option))

                        dismiss()
                    },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * One genre-sheet row: hairline divider, `titleLarge` italic label (promoted to `primary` when
 * [active]), and a trailing `primary` check glyph when active — `ShelvesSheetRow`'s row anatomy,
 * minus the leading shelf-type glyph and trailing item count neither applies to a genre.
 */
@Composable
private fun BecauseYouReadGenreSheetRow(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pointerHandCursor()
                .pressScaleClickable(onClick = onClick)
                .padding(vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.editorialTypography.titleLarge.copy(fontStyle = FontStyle.Italic),
                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            if (active) {
                val checkIcon = drawableIconResource(
                    icon = SoftcoverIcon.Check,
                    contentDescription = "Current genre",
                )

                Icon(
                    painter = checkIcon.getIconPainter(),
                    contentDescription = checkIcon.contentDescription,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
// endregion

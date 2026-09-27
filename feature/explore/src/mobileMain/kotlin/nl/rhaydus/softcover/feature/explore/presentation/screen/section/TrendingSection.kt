package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.rememberStaggeredEntryCoordinator
import nl.rhaydus.designsystem.component.staggeredEntry
import nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader
import nl.rhaydus.designsystem.util.SkeletonCrossfade
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_TRENDING
import nl.rhaydus.softcover.feature.explore.presentation.screen.TRENDING_SKELETON_COUNT

private val trendingListState = LazyListState()

private val TRENDING_CARD_WIDTH = 118.dp

@Composable
internal fun TrendingSection(
    books: List<Book>,
    covers: Map<Int, CoverUiModel>,
    unreleasedBadges: Map<Int, BadgeUiModel>,
    isLoading: Boolean,
    onBookClick: (Book, String?) -> Unit,
) {
    if (isLoading.not() && books.isEmpty()) return

    // Hoisted above the crossfade so the coordinator's first-composition timestamp is stamped as
    // soon as this section mounts - typically while still loading - not at the moment content
    // arrives. By the time a real network fetch resolves, the stagger window has usually already
    // elapsed, so the item entrance defers entirely to the crossfade below instead of double-
    // animating alongside it; the stagger only plays when content is already on hand at the
    // section's very first paint (design-system.md §2.5 "Staggered entry on first composition").
    val entry = rememberStaggeredEntryCoordinator(key = "explore:trending")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        EditorialSectionHeader(
            eyebrow = "Trending this week",
            headline = "What everyone's reading",
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        SkeletonCrossfade(
            isLoading = isLoading,
            label = "TrendingRail",
        ) { loading ->
            if (loading) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(TRENDING_SKELETON_COUNT) {
                        TrendingCardSkeleton(modifier = Modifier.width(TRENDING_CARD_WIDTH))
                    }
                }
            } else {
                LazyRow(
                    state = trendingListState,
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    itemsIndexed(books, key = { _, book -> book.id }) { index, book ->
                        val cover = covers[book.id]

                        TrendingCard(
                            modifier = Modifier
                                .width(TRENDING_CARD_WIDTH)
                                .staggeredEntry(coordinator = entry, index = index),
                            book = book,
                            cover = cover,
                            unreleasedBadge = unreleasedBadges[book.id],
                            onClick = {
                                onBookClick(
                                    book,
                                    SURFACE_TRENDING,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

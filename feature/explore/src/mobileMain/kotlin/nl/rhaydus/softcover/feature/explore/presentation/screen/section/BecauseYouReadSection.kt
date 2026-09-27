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
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.screen.BECAUSE_YOU_READ_SKELETON_COUNT
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_BECAUSE_YOU_READ

private val becauseYouReadListState = LazyListState()

private val BECAUSE_YOU_READ_CARD_WIDTH = 118.dp

@Composable
internal fun BecauseYouReadSection(
    genre: String?,
    genreOptions: List<String>,
    books: List<Book>,
    covers: Map<Int, CoverUiModel>,
    unreleasedBadges: Map<Int, BadgeUiModel>,
    isLoading: Boolean,
    onBookClick: (Book, String?) -> Unit,
    runAction: (ExploreAction) -> Unit,
) {
    // Terminal empty: the fetch is done and this reader has no genre to recommend from (or,
    // defensively, no books despite a resolved genre) - nothing to show, so the section collapses
    // with no reserved space. Any other state - still loading, regardless of whether genre/books
    // happen to have arrived yet - keeps the header+rail's full footprint reserved below, which is
    // what fixes the section popping into the feed at full height once the genre resolves: it used
    // to gate on `if (genre == null) return` unconditionally, hiding the whole section - header
    // included - for the entire loading window, not just the genuinely-empty terminal state.
    if (isLoading.not() && (genre == null || books.isEmpty())) return

    // Hoisted above the crossfade - see TrendingSection's comment for why.
    val entry = rememberStaggeredEntryCoordinator(key = "explore:because_you_read")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SkeletonCrossfade(
            isLoading = isLoading,
            modifier = Modifier.padding(horizontal = 24.dp),
            label = "BecauseYouReadHeader",
        ) { loading ->
            if (loading) {
                EditorialSectionHeaderSkeleton()
            } else if (genre != null) {
                EditorialSectionHeader(
                    eyebrow = "Because you read",
                    headline = genre,
                )
            }
        }

        // Crossfaded too, not just gated on `genre != null` directly: genre and `isLoading` flip
        // together (see the guard above), so an ungated appearance would hard-pop this control in
        // alongside the header/rail's smooth 150ms fade — a small but visible inconsistency next
        // to the very transition this file's crossfade work exists to smooth out.
        SkeletonCrossfade(
            isLoading = isLoading,
            modifier = Modifier.padding(horizontal = 24.dp),
            label = "BecauseYouReadGenreControl",
        ) { loading ->
            if (loading.not() && genre != null) {
                BecauseYouReadGenreControl(
                    genre = genre,
                    options = genreOptions,
                    runAction = runAction,
                )
            }
        }

        SkeletonCrossfade(
            isLoading = isLoading,
            label = "BecauseYouReadRail",
        ) { loading ->
            if (loading) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(BECAUSE_YOU_READ_SKELETON_COUNT) {
                        BecauseYouReadCardSkeleton(modifier = Modifier.width(BECAUSE_YOU_READ_CARD_WIDTH))
                    }
                }
            } else {
                LazyRow(
                    state = becauseYouReadListState,
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    itemsIndexed(books, key = { _, book -> book.id }) { index, book ->
                        val cover = covers[book.id]

                        BecauseYouReadCard(
                            modifier = Modifier
                                .width(BECAUSE_YOU_READ_CARD_WIDTH)
                                .staggeredEntry(coordinator = entry, index = index),
                            book = book,
                            cover = cover,
                            unreleasedBadge = unreleasedBadges[book.id],
                            onClick = {
                                onBookClick(
                                    book,
                                    SURFACE_BECAUSE_YOU_READ,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

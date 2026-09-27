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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import nl.rhaydus.softcover.feature.explore.presentation.screen.CONTINUE_SERIES_SKELETON_COUNT
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_UP_NEXT

private val continueSeriesListState = LazyListState()

private val UP_NEXT_CARD_WIDTH = 126.dp

@Composable
internal fun ContinueSeriesSection(
    books: List<Book>,
    covers: Map<Int, CoverUiModel>,
    unreleasedBadges: Map<Int, BadgeUiModel>,
    isLoading: Boolean,
    onBookClick: (Book, String?) -> Unit,
    runAction: (ExploreAction) -> Unit,
) {
    if (isLoading.not() && books.isEmpty()) return

    var sheetBook by remember { mutableStateOf<Book?>(null) }

    // Hoisted above the crossfade - see TrendingSection's comment for why.
    val entry = rememberStaggeredEntryCoordinator(key = "explore:continue_series")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        EditorialSectionHeader(
            eyebrow = "Pick up where you left off",
            headline = "Up next in your series",
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        SkeletonCrossfade(
            isLoading = isLoading,
            label = "ContinueSeriesRail",
        ) { loading ->
            if (loading) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(CONTINUE_SERIES_SKELETON_COUNT) {
                        SeriesCardSkeleton(modifier = Modifier.width(UP_NEXT_CARD_WIDTH))
                    }
                }
            } else {
                LazyRow(
                    state = continueSeriesListState,
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    itemsIndexed(books, key = { _, book -> book.id }) { index, book ->
                        val cover = covers[book.id]
                        val cardModifier = Modifier
                            .width(UP_NEXT_CARD_WIDTH)
                            .staggeredEntry(coordinator = entry, index = index)

                        if (book.isUnreleased) {
                            UnreleasedSeriesCard(
                                modifier = cardModifier,
                                book = book,
                                cover = cover,
                                unreleasedBadge = unreleasedBadges[book.id],
                                onClick = {
                                    onBookClick(
                                        book,
                                        SURFACE_UP_NEXT,
                                    )
                                },
                                onMenuClick = { sheetBook = book },
                            )
                        } else {
                            SeriesCard(
                                modifier = cardModifier,
                                book = book,
                                cover = cover,
                                onClick = {
                                    onBookClick(
                                        book,
                                        SURFACE_UP_NEXT,
                                    )
                                },
                                onMenuClick = { sheetBook = book },
                            )
                        }
                    }
                }
            }
        }
    }

    sheetBook?.let { book ->
        ContinueSeriesMenuSheet(
            book = book,
            runAction = runAction,
            onDismiss = { sheetBook = null },
        )
    }
}

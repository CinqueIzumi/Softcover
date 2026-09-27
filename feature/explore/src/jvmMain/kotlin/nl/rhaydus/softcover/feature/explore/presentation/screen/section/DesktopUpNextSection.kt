package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.util.SkeletonCrossfade
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_UP_NEXT

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DesktopUpNextSection(
    books: List<Book>,
    covers: Map<Int, CoverUiModel>,
    unreleasedBadges: Map<Int, BadgeUiModel>,
    isLoading: Boolean,
    onBookClick: (Book, String?) -> Unit,
    runAction: (ExploreAction) -> Unit,
) {
    if (isLoading.not() && books.isEmpty()) return

    var sheetBook by remember { mutableStateOf<Book?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeaderBar(
            eyebrow = "Pick up where you left off",
            headline = "Up next in your series",
        )

        SkeletonCrossfade(
            isLoading = isLoading,
            label = "DesktopUpNextRail",
        ) { loading ->
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                if (loading) {
                    repeat(DESKTOP_DISCOVERY_SKELETON_COUNT) {
                        SeriesCardSkeleton(modifier = Modifier.width(DESKTOP_UP_NEXT_CARD_WIDTH))
                    }
                } else {
                    books.forEach { book ->
                        val cover = covers[book.id]
                        val cardModifier = Modifier.width(DESKTOP_UP_NEXT_CARD_WIDTH)

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

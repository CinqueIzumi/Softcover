package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.util.SkeletonCrossfade
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_TRENDING

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DesktopTrendingSection(
    books: List<Book>,
    covers: Map<Int, CoverUiModel>,
    unreleasedBadges: Map<Int, BadgeUiModel>,
    isLoading: Boolean,
    onBookClick: (Book, String?) -> Unit,
) {
    if (isLoading.not() && books.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeaderBar(
            eyebrow = "Trending this week",
            headline = "What everyone's reading",
        )

        SkeletonCrossfade(
            isLoading = isLoading,
            label = "DesktopTrendingRail",
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
                        TrendingCardSkeleton(modifier = Modifier.width(DESKTOP_TRENDING_CARD_WIDTH))
                    }
                } else {
                    books.forEach { book ->
                        val cover = covers[book.id]

                        TrendingCard(
                            modifier = Modifier.width(DESKTOP_TRENDING_CARD_WIDTH),
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

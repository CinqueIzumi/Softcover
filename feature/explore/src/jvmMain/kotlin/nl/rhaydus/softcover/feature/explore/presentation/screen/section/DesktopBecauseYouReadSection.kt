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
import nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader
import nl.rhaydus.designsystem.util.SkeletonCrossfade
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_BECAUSE_YOU_READ

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DesktopBecauseYouReadSection(
    genre: String?,
    genreOptions: List<String>,
    books: List<Book>,
    covers: Map<Int, CoverUiModel>,
    unreleasedBadges: Map<Int, BadgeUiModel>,
    isLoading: Boolean,
    onBookClick: (Book, String?) -> Unit,
    runAction: (ExploreAction) -> Unit,
) {
    // See the mobile BecauseYouReadSection's comment: the section only collapses once the fetch
    // is done and there's genuinely nothing to show (no genre, or no books despite a genre) -
    // any other state, including "still loading with genre unresolved", keeps the header+rail's
    // full footprint reserved so the section never pops into the feed at full height once the
    // genre resolves.
    if (isLoading.not() && (genre == null || books.isEmpty())) return

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SkeletonCrossfade(
            isLoading = isLoading,
            modifier = Modifier.padding(horizontal = 24.dp),
            label = "DesktopBecauseYouReadHeader",
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

        // Crossfaded too, not just gated on `genre != null` directly - see the mobile
        // BecauseYouReadSection's comment: genre and `isLoading` flip together, so an ungated
        // appearance would hard-pop this control in alongside the header/rail's smooth fade.
        SkeletonCrossfade(
            isLoading = isLoading,
            modifier = Modifier.padding(horizontal = 24.dp),
            label = "DesktopBecauseYouReadGenreControl",
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
            label = "DesktopBecauseYouReadRail",
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
                        BecauseYouReadCardSkeleton(modifier = Modifier.width(DESKTOP_TRENDING_CARD_WIDTH))
                    }
                } else {
                    books.forEach { book ->
                        val cover = covers[book.id]

                        BecauseYouReadCard(
                            modifier = Modifier.width(DESKTOP_TRENDING_CARD_WIDTH),
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

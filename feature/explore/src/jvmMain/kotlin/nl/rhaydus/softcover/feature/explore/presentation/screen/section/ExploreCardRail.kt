package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateBounds
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.component.bookcard.bookCardHandOver
import nl.rhaydus.softcover.core.component.bookcard.lazyKey
import nl.rhaydus.softcover.core.component.bookcard.rememberBookCardEntryCoordinator
import nl.rhaydus.softcover.core.component.state.SkeletonRevealGroup
import nl.rhaydus.softcover.core.component.transition.ExitingItems
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.domain.model.Book

@OptIn(
    ExperimentalLayoutApi::class,
    ExperimentalSharedTransitionApi::class,
    ExperimentalMaterial3ExpressiveApi::class,
)
@Composable
internal fun ExploreCardRail(
    rail: ExploreRail,
    set: BookCardSet<Book>,
    onCardEvent: (BookCardEvent) -> Unit,
) {
    val entry = rememberBookCardEntryCoordinator(
        cards = set.cards,
        key = rail.staggerKey,
    )

    val cardWidth = when (rail) {
        ExploreRail.UP_NEXT -> 150.dp
        ExploreRail.BECAUSE_YOU_READ, ExploreRail.TRENDING -> 168.dp
    }

    val placementSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Rect>()

    SkeletonRevealGroup {
        LookaheadScope {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.gutter),
                horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.xl),
            ) {
                ExitingItems(
                    items = set.cards,
                    keyOf = { it.key.lazyKey },
                    animatesExit = { it.isLoading.not() },
                    itemModifier = Modifier.animateBounds(
                        lookaheadScope = this@LookaheadScope,
                        boundsTransform = { _, _ -> placementSpec },
                    ),
                ) { card, index ->
                    BookCard(
                        model = card,
                        onEvent = onCardEvent,
                        modifier = Modifier
                            .width(cardWidth)
                            .bookCardHandOver(
                                card = card,
                                coordinator = entry,
                                index = index,
                            ),
                    )
                }
            }
        }
    }
}

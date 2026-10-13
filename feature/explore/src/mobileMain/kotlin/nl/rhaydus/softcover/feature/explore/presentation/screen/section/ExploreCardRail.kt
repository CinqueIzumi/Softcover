package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.rememberLazyItemMutationAnimator
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.bookCardHandOver
import nl.rhaydus.softcover.core.component.bookcard.lazyKey
import nl.rhaydus.softcover.core.component.bookcard.rememberBookCardEntryCoordinator
import nl.rhaydus.softcover.core.component.state.SkeletonRevealGroup
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.domain.model.Book

private val railListStates = ExploreRail.entries.associateWith { LazyListState() }

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

    val animator = rememberLazyItemMutationAnimator(
        keys = remember(set.cards) {
            set.cards.filterNot(BookCardUiModel::isLoading).map { it.key.lazyKey }
        },
    )

    val listState = railListStates.getValue(rail)

    val railKeys = remember(set.cards) { set.cards.map { it.key.lazyKey } }

    val anchoredKeys = remember { mutableListOf<Any>() }

    SideEffect {
        if (anchoredKeys == railKeys) return@SideEffect

        // Without this, LazyRow follows the old first card's key, so a card inserted at index 0 lands off-screen.
        if (listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0) {
            listState.requestScrollToItem(index = 0)
        }

        anchoredKeys.clear()
        anchoredKeys.addAll(railKeys)
    }

    val cardWidth = when (rail) {
        ExploreRail.UP_NEXT -> 126.dp
        ExploreRail.BECAUSE_YOU_READ, ExploreRail.TRENDING -> 118.dp
    }

    SkeletonRevealGroup {
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = Spacing.gutter),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            itemsIndexed(set.cards, key = { _, card -> card.key.lazyKey }) { index, card ->
                BookCard(
                    model = card,
                    onEvent = onCardEvent,
                    modifier = Modifier
                        .width(cardWidth)
                        .bookCardHandOver(
                            scope = this,
                            card = card,
                            animator = animator,
                            coordinator = entry,
                            index = index,
                        ),
                )
            }
        }
    }
}

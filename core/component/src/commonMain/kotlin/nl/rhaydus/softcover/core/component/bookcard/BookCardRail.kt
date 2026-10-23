package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.component.rememberLazyItemMutationAnimator
import nl.rhaydus.softcover.core.component.state.SkeletonRevealGroup
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing

/**
 * A horizontally scrolling rail of book cards. [listState] is the caller's, so a rail keeps its scroll
 * position across navigation.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <P> BookCardRail(
    model: BookCardRailUiModel<P>,
    onEvent: (BookCardRailEvent) -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val cards = model.set.cards

    val entry = rememberBookCardEntryCoordinator(
        cards = cards,
        key = model.staggerKey,
    )

    val animator = rememberLazyItemMutationAnimator(
        keys = remember(cards) {
            cards.filterNot(BookCardUiModel::isLoading).map { it.key.lazyKey }
        },
    )

    val railKeys = remember(cards) { cards.map { it.key.lazyKey } }

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

    BookCardRailNearEndEffect(
        listState = listState,
        cardCount = cards.size,
        isAppending = model.isAppending,
        canLoadMore = model.canLoadMore,
        showsStandIns = cards.any(BookCardUiModel::isLoading),
        onNearEnd = { onEvent(BookCardRailEvent.NearEnd) },
    )

    val onCardEvent = remember(onEvent) {
        { event: BookCardEvent -> onEvent(BookCardRailEvent.Card(event)) }
    }

    SkeletonRevealGroup {
        LazyRow(
            modifier = modifier,
            state = listState,
            contentPadding = PaddingValues(horizontal = model.horizontalInset),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            itemsIndexed(cards, key = { _, card -> card.key.lazyKey }) { index, card ->
                BookCard(
                    model = card,
                    onEvent = onCardEvent,
                    modifier = Modifier
                        .width(model.cardWidth)
                        .bookCardHandOver(
                            scope = this,
                            card = card,
                            animator = animator,
                            coordinator = entry,
                            index = index,
                        ),
                )
            }

            if (model.isAppending) {
                item(key = APPENDING_ITEM_KEY) {
                    Box(
                        modifier = Modifier
                            .width(model.cardWidth)
                            .aspectRatio(COVER_ASPECT_RATIO),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularWavyProgressIndicator()
                    }
                }
            }
        }
    }
}

private const val APPENDING_ITEM_KEY = "appending"
private const val COVER_ASPECT_RATIO = 2f / 3f

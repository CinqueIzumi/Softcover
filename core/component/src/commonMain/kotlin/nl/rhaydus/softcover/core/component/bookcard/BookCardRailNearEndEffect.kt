package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow

/**
 * Calls [onNearEnd] on each scroll frame the visible window ends within [NEAR_END_THRESHOLD] cards of the
 * last real card. The consumer owns the re-entrancy guard, so this fires freely; it is silent while a page is
 * appending, stand-ins show or [canLoadMore] is false.
 */
@Composable
internal fun BookCardRailNearEndEffect(
    listState: LazyListState,
    cardCount: Int,
    isAppending: Boolean,
    canLoadMore: Boolean,
    showsStandIns: Boolean,
    onNearEnd: () -> Unit,
) {
    val latestOnNearEnd by rememberUpdatedState(onNearEnd)

    LaunchedEffect(cardCount, isAppending, showsStandIns, canLoadMore) {
        if (cardCount == 0 || isAppending || showsStandIns || canLoadMore.not()) return@LaunchedEffect

        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= cardCount - NEAR_END_THRESHOLD) {
                    latestOnNearEnd()
                }
            }
    }
}

private const val NEAR_END_THRESHOLD = 4

package nl.rhaydus.softcover.core.presentation.util

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import cafe.adriel.voyager.navigator.tab.Tab
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import org.koin.compose.koinInject

/**
 * [onReselect] returns whether it handled the tap, for example by scrolling a list to the top; `false`
 * lets the nav chrome pulse the tab's icon instead.
 */
@Composable
fun TabReselectEffect(
    tab: Tab,
    onReselect: suspend () -> Boolean,
) {
    val registry = koinInject<TabReselectRegistry>()
    val currentOnReselect by rememberUpdatedState(onReselect)

    DisposableEffect(registry, tab) {
        val handler: suspend () -> Boolean = { currentOnReselect() }
        registry.register(
            tab,
            handler,
        )

        onDispose {
            registry.unregister(
                tab,
                handler,
            )
        }
    }
}

/**
 * Scrolls to the top on a reselect through [scrollToTop], which receives whether to animate and returns
 * whether it scrolled. Use it when the scrolling state depends on the screen's phase.
 */
@Composable
fun TabReselectScrollToTopEffect(
    tab: Tab,
    scrollToTop: suspend (animate: Boolean) -> Boolean,
) {
    val animate = playDecorativeMotion()

    TabReselectEffect(tab) { scrollToTop(animate) }
}

@Composable
fun TabReselectScrollToTopEffect(
    tab: Tab,
    state: ScrollState,
) {
    TabReselectScrollToTopEffect(tab) { animate -> state.scrollToTop(animate) }
}

@Composable
fun TabReselectScrollToTopEffect(
    tab: Tab,
    state: LazyListState,
) {
    TabReselectScrollToTopEffect(tab) { animate -> state.scrollToTop(animate) }
}

@Composable
fun TabReselectScrollToTopEffect(
    tab: Tab,
    state: LazyGridState,
) {
    TabReselectScrollToTopEffect(tab) { animate -> state.scrollToTop(animate) }
}

suspend fun ScrollState.scrollToTop(animate: Boolean): Boolean {
    if (canScrollBackward.not()) return false

    if (animate) animateScrollTo(0) else scrollTo(0)
    return true
}

suspend fun LazyListState.scrollToTop(animate: Boolean): Boolean {
    if (canScrollBackward.not()) return false

    if (animate) animateScrollToItem(0) else scrollToItem(0)
    return true
}

suspend fun LazyGridState.scrollToTop(animate: Boolean): Boolean {
    if (canScrollBackward.not()) return false

    if (animate) animateScrollToItem(0) else scrollToItem(0)
    return true
}

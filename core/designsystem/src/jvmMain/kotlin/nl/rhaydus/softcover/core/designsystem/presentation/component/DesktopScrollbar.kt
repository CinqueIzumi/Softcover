package nl.rhaydus.softcover.core.designsystem.presentation.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Desktop affordance: the canonical vertical scrollbar for a long [LazyVerticalGrid][androidx.compose.foundation.lazy.grid.LazyVerticalGrid].
 * Mouse-driven desktop scrolling has no fling indicator, so a persistent scrollbar is how a desktop
 * user reads (and grabs) their position in a long list. Overlay it on the trailing edge of the grid
 * — e.g. `Modifier.align(Alignment.CenterEnd).fillMaxHeight()` inside the `Box` that wraps the grid —
 * driven by the same [LazyGridState] the grid scrolls on.
 *
 * jvm-only: there is no scrollbar on touch platforms (the fling + edge-glow is the affordance), so
 * this lives in `jvmMain` and is called only from the bespoke desktop screen layouts.
 */
@Composable
fun DesktopVerticalScrollbar(
    gridState: LazyGridState,
    modifier: Modifier = Modifier,
) {
    VerticalScrollbar(
        adapter = rememberScrollbarAdapter(scrollState = gridState),
        modifier = modifier,
        style = softcoverScrollbarStyle(),
    )
}

/**
 * [LazyColumn][androidx.compose.foundation.lazy.LazyColumn] counterpart of the grid scrollbar above —
 * same desktop rationale and same trailing-edge overlay placement, driven by the [LazyListState] the
 * list scrolls on.
 */
@Composable
fun DesktopVerticalScrollbar(
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    VerticalScrollbar(
        adapter = rememberScrollbarAdapter(scrollState = listState),
        modifier = modifier,
        style = softcoverScrollbarStyle(),
    )
}

/**
 * [verticalScroll][androidx.compose.foundation.verticalScroll] counterpart driven by a [ScrollState].
 * Prefer this over the [LazyListState] overload when the content is short and its items vary wildly in
 * height (e.g. the desktop Reading list, where a tall featured card sits above short rows): a lazy
 * adapter only *estimates* total height from the average measured-item size, so the thumb jumps as the
 * tall item scrolls out of view, whereas a fully-measured [ScrollState] reports the exact extent and
 * keeps the thumb stable.
 */
@Composable
fun DesktopVerticalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
) {
    VerticalScrollbar(
        adapter = rememberScrollbarAdapter(scrollState = scrollState),
        modifier = modifier,
        style = softcoverScrollbarStyle(),
    )
}

/**
 * Compose Desktop's default [ScrollbarStyle] paints a near-black thumb (`unhoverColor` is black at
 * ~12% alpha), which is invisible on the app's dark editorial surfaces. This keys the thumb to the
 * theme's `onSurface` so it reads on both light and dark, brightening on hover.
 */
@Composable
private fun softcoverScrollbarStyle(): ScrollbarStyle {
    val onSurface = MaterialTheme.colorScheme.onSurface

    return ScrollbarStyle(
        minimalHeight = 24.dp,
        thickness = 8.dp,
        shape = RoundedCornerShape(4.dp),
        hoverDurationMillis = 300,
        unhoverColor = onSurface.copy(alpha = 0.28f),
        hoverColor = onSurface.copy(alpha = 0.50f),
    )
}

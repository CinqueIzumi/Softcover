package nl.rhaydus.softcover.core.designsystem.presentation.component

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

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
    )
}

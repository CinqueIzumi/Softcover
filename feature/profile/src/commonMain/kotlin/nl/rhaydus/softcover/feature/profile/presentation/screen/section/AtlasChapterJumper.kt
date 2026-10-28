package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nl.rhaydus.softcover.feature.profile.presentation.state.AtlasChapter
import kotlin.math.roundToInt

/**
 * Scrolls the Atlas page to a chapter. Chapters and the scroll viewport report where they sit; the offset
 * is worked out from those positions when a jump is requested, so it is right at whatever scroll the
 * reader has reached.
 */
@Stable
internal class AtlasChapterJumper(
    private val scrollState: ScrollState,
    private val coroutineScope: CoroutineScope,
) {
    private val chapters = mutableMapOf<AtlasChapter, LayoutCoordinates>()

    private var viewportTop = 0f

    /** Marks the scroll viewport; apply it before the `verticalScroll` so it measures the fixed window. */
    fun viewport(): Modifier = Modifier.onGloballyPositioned { viewportTop = it.positionInRoot().y }

    /** Marks the chrome sitting directly above the scroll viewport when the viewport itself is out of reach. */
    fun aboveViewport(): Modifier = Modifier.onGloballyPositioned {
        viewportTop = it.positionInRoot().y + it.size.height
    }

    fun chapter(chapter: AtlasChapter): Modifier = Modifier.onGloballyPositioned { chapters[chapter] = it }

    fun jumpTo(chapter: AtlasChapter) {
        val coordinates = chapters[chapter]?.takeIf { it.isAttached } ?: return
        val target = scrollState.value + (coordinates.positionInRoot().y - viewportTop).roundToInt()

        coroutineScope.launch { scrollState.animateScrollTo(target) }
    }
}

@Composable
internal fun rememberAtlasChapterJumper(scrollState: ScrollState): AtlasChapterJumper {
    val coroutineScope = rememberCoroutineScope()

    return remember(scrollState, coroutineScope) {
        AtlasChapterJumper(
            scrollState = scrollState,
            coroutineScope = coroutineScope,
        )
    }
}

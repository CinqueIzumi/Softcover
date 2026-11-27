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

    fun recordViewportTop(top: Float) {
        viewportTop = top
    }

    fun recordChapter(
        chapter: AtlasChapter,
        coordinates: LayoutCoordinates,
    ) {
        chapters[chapter] = coordinates
    }

    fun jumpTo(chapter: AtlasChapter) {
        val coordinates = chapters[chapter]?.takeIf { it.isAttached } ?: return

        val target = scrollState.value + (coordinates.positionInRoot().y - viewportTop).roundToInt()

        coroutineScope.launch { scrollState.animateScrollTo(target) }
    }
}

/** Marks the scroll viewport; apply it before the `verticalScroll` so it measures the fixed window. */
internal fun Modifier.atlasViewport(jumper: AtlasChapterJumper): Modifier = onGloballyPositioned {
    jumper.recordViewportTop(it.positionInRoot().y)
}

/** Marks the chrome sitting directly above the scroll viewport when the viewport itself is out of reach. */
internal fun Modifier.atlasAboveViewport(jumper: AtlasChapterJumper): Modifier = onGloballyPositioned {
    jumper.recordViewportTop(it.positionInRoot().y + it.size.height)
}

internal fun Modifier.atlasChapter(
    jumper: AtlasChapterJumper,
    chapter: AtlasChapter,
): Modifier = onGloballyPositioned {
    jumper.recordChapter(
        chapter,
        it,
    )
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

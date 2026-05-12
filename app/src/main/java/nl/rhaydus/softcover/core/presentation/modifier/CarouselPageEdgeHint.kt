package nl.rhaydus.softcover.core.presentation.modifier

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import nl.rhaydus.softcover.core.presentation.util.playDecorativeMotion

private const val HOLD_DURATION_MS = 1_000L
private const val FADE_OUT_DURATION_MS = 400

private val shownCarouselKeys = mutableSetOf<Any>()

/**
 * Paints a 2dp page-edge hint under the rightmost partially-visible card of a carousel on
 * its first composition for the lifetime of the process. Held for 1s, then fades over
 * ~400ms. Tracked per-[key] in a process-wide registry — re-entries (push-and-back, tab
 * swap, config change) render statically. State clears only on process death.
 *
 * Gated by `playDecorativeMotion`: when system animations are disabled the hint is a no-op
 * and the key is **not** marked shown, so the user still gets it on the next launch with
 * animations re-enabled.
 *
 * Apply to book carousels — not chip-strip rows, whose full content is already inline.
 */
@Composable
fun Modifier.carouselPageEdgeHint(
    state: LazyListState,
    key: Any,
    color: Color = MaterialTheme.colorScheme.primary,
): Modifier {
    val playMotion = playDecorativeMotion()

    val barHeightPx = with(LocalDensity.current) { 2.dp.toPx() }

    val rightmostPartial by remember(state) {
        derivedStateOf {
            val info = state.layoutInfo

            if (info.totalItemsCount == 0) return@derivedStateOf null

            val viewportEnd = info.viewportEndOffset

            info.visibleItemsInfo.lastOrNull { item ->
                item.offset + item.size > viewportEnd
            }
        }
    }

    val alpha = remember { Animatable(initialValue = 0f) }

    val hasPartial = rightmostPartial != null

    LaunchedEffect(hasPartial, playMotion, key) {
        if (playMotion.not()) return@LaunchedEffect
        if (hasPartial.not()) return@LaunchedEffect
        if (key in shownCarouselKeys) return@LaunchedEffect

        shownCarouselKeys += key

        alpha.snapTo(targetValue = 1f)
        delay(timeMillis = HOLD_DURATION_MS)

        alpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = FADE_OUT_DURATION_MS),
        )
    }

    return this.drawWithContent {
        drawContent()

        val partial = rightmostPartial
        val currentAlpha = alpha.value

        if (partial == null || currentAlpha <= 0f) return@drawWithContent

        drawRect(
            color = color.copy(alpha = currentAlpha),
            topLeft = Offset(
                x = partial.offset.toFloat(),
                y = size.height - barHeightPx,
            ),
            size = Size(
                width = partial.size.toFloat(),
                height = barHeightPx,
            ),
        )
    }
}

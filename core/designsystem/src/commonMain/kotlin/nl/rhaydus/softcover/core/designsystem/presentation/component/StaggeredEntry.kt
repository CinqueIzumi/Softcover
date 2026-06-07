package nl.rhaydus.softcover.core.designsystem.presentation.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.time.Clock
import kotlinx.coroutines.delay
import nl.rhaydus.softcover.core.designsystem.presentation.util.playDecorativeMotion

private const val DEFAULT_STEP_MS = 60
private const val DEFAULT_WINDOW_MS = 350
private const val DEFAULT_FADE_MS = 240
private val DEFAULT_TRANSLATE = 8.dp

/**
 * Plays a brief upward-translate + fade-in for items appearing in a lazy carousel or list
 * on the screen's *first* entry only. Subsequent re-entries (push-to-detail-and-back, tab
 * swap, config change) render statically — the stagger is an introduction, not a transition.
 *
 * "First entry" is tracked per-[key] in a process-wide registry. Callers pass a stable
 * identifier (a Voyager `Screen` object is ideal — it's a singleton with reference
 * equality). State clears only on process death, which matches the intent ("introduce
 * once per app lifetime").
 *
 * Items composed within [windowMillis] of the first-entry timestamp stagger by [stepMillis]
 * per index; items composed later (scrolled into view, mutated in after a network update,
 * etc.) render statically.
 *
 * The roadmap explicitly rules out "animate everything on scroll" — this is a one-shot,
 * once-per-screen-lifetime effect. Pair the coordinator with [staggeredEntry] on each item.
 *
 * Gated by [playDecorativeMotion]: when system animations are disabled the modifier is a
 * no-op.
 *
 * Slow-device caveat: the window is measured in wall-clock time, so on a device where
 * lazy-item composition spreads across many frames an item that *is* in the initial
 * viewport but composes after the window elapses will render statically. Widen
 * [windowMillis] only if a real device shows visible items dropping out of the stagger —
 * the default leaves headroom for typical lazy-grid composition.
 */
@Stable
class StaggeredEntryCoordinator internal constructor(
    internal val stepMillis: Int,
    internal val windowMillis: Int,
    internal val playMotion: Boolean,
    internal val startMillis: Long,
)

private val firstEntryTimestamps = mutableMapOf<Any, Long>()

@Composable
fun rememberStaggeredEntryCoordinator(
    key: Any,
    stepMillis: Int = DEFAULT_STEP_MS,
    windowMillis: Int = DEFAULT_WINDOW_MS,
): StaggeredEntryCoordinator {
    val playMotion = playDecorativeMotion()

    return remember(key, playMotion, stepMillis, windowMillis) {
        val startMillis = firstEntryTimestamps.getOrPut(key) { Clock.System.now().toEpochMilliseconds() }

        StaggeredEntryCoordinator(
            stepMillis = stepMillis,
            windowMillis = windowMillis,
            playMotion = playMotion,
            startMillis = startMillis,
        )
    }
}

@Composable
fun Modifier.staggeredEntry(
    coordinator: StaggeredEntryCoordinator,
    index: Int,
    translateFrom: Dp = DEFAULT_TRANSLATE,
): Modifier {
    val shouldPlay = remember(coordinator) {
        coordinator.playMotion &&
            (Clock.System.now().toEpochMilliseconds() - coordinator.startMillis) < coordinator.windowMillis
    }

    if (shouldPlay.not()) return this

    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay((index.coerceAtLeast(0) * coordinator.stepMillis).toLong())
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = DEFAULT_FADE_MS),
        )
    }

    val density = LocalDensity.current

    val translatePx = with(density) { translateFrom.toPx() }

    return this.graphicsLayer {
        val value = progress.value
        alpha = value
        translationY = translatePx * (1f - value)
    }
}

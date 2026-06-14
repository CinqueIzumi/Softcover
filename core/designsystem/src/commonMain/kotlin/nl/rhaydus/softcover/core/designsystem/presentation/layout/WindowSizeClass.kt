package nl.rhaydus.softcover.core.designsystem.presentation.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The measured size of the surface the app is rendered into, bucketed into a [WindowWidthClass].
 *
 * Branch layout decisions on [widthClass] — it is the single source of the app's breakpoints, so
 * no screen hardcodes a dp threshold. [widthDp] / [heightDp] expose the measured values for the
 * rare continuous sizing decision (e.g. a hero whose height tracks the viewport); reach for the
 * class first.
 */
@Immutable
data class WindowSizeClass(
    val widthClass: WindowWidthClass,
    val widthDp: Dp,
    val heightDp: Dp,
) {
    companion object {
        /** Upper bound (exclusive) of [WindowWidthClass.COMPACT]. */
        val COMPACT_MAX_WIDTH = 600.dp

        /** Upper bound (exclusive) of [WindowWidthClass.MEDIUM]. */
        val MEDIUM_MAX_WIDTH = 840.dp

        /**
         * Buckets a measured width into a [WindowWidthClass]. The single place the breakpoints are
         * applied — both [rememberWindowSizeClass] and tests go through here.
         */
        fun widthClassFor(widthDp: Dp): WindowWidthClass = when {
            widthDp < COMPACT_MAX_WIDTH -> WindowWidthClass.COMPACT
            widthDp < MEDIUM_MAX_WIDTH -> WindowWidthClass.MEDIUM
            else -> WindowWidthClass.EXPANDED
        }
    }
}

/**
 * Reads the current window size from [LocalWindowInfo] and buckets its width into a
 * [WindowSizeClass]. Recomputes whenever the container size changes, so it tracks desktop window
 * resizes and foldable posture changes for free.
 */
@Composable
fun rememberWindowSizeClass(): WindowSizeClass {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current

    return remember(windowInfo.containerSize, density) {
        val widthDp = with(density) { windowInfo.containerSize.width.toDp() }
        val heightDp = with(density) { windowInfo.containerSize.height.toDp() }

        WindowSizeClass(
            widthClass = WindowSizeClass.widthClassFor(widthDp),
            widthDp = widthDp,
            heightDp = heightDp,
        )
    }
}

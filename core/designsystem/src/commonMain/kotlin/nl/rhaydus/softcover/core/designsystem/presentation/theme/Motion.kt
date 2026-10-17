package nl.rhaydus.softcover.core.designsystem.presentation.theme

import androidx.compose.animation.core.DurationBasedAnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * The motion specs the app owns because the expressive `MotionScheme` has none for them.
 * [SoftcoverTheme] provides [Full] or [Reduced], read through [MaterialTheme.motion].
 */
@Immutable
class Motion internal constructor(
    /** A spec slow enough to follow a value as it changes; the full one settles in about 0.9 s without overshoot. */
    val valueChange: FiniteAnimationSpec<Float>,
    private val animates: Boolean,
) {
    fun celebration(durationMillis: Int): FiniteAnimationSpec<Float> = timed(durationMillis)

    fun ambientLoop(durationMillis: Int): DurationBasedAnimationSpec<Float> = timed(durationMillis)

    private fun timed(durationMillis: Int): DurationBasedAnimationSpec<Float> = if (animates) {
        tween(
            durationMillis = durationMillis,
            easing = FastOutSlowInEasing,
        )
    } else {
        snap()
    }

    internal companion object {
        val Full = Motion(
            valueChange = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessVeryLow,
                visibilityThreshold = 0.01f,
            ),
            animates = true,
        )

        val Reduced = Motion(
            valueChange = snap(),
            animates = false,
        )
    }
}

internal val LocalMotion = staticCompositionLocalOf { Motion.Full }

val MaterialTheme.motion: Motion
    @Composable
    @ReadOnlyComposable
    get() = LocalMotion.current

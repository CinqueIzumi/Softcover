package nl.rhaydus.softcover.core.component.modifier

import androidx.compose.animation.core.Animatable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.designsystem.nav.NavPulse

private const val DEFAULT_PULSE_PEAK_SCALE = 1.22f

@Composable
fun rememberPulseScale(
    trigger: Int,
    peakScale: Float = DEFAULT_PULSE_PEAK_SCALE,
): Float = rememberPulseScaleProvider(
    trigger = trigger,
    peakScale = peakScale,
)()

/** Same pulse as [rememberPulseScale], read through a lambda so a `graphicsLayer {}` block tracks it without recomposing. */
@Composable
fun rememberPulseScaleProvider(
    trigger: Int,
    peakScale: Float = DEFAULT_PULSE_PEAK_SCALE,
): () -> Float {
    val playMotion = playDecorativeMotion()
    val firstTrigger = remember { trigger }
    val scale = remember { Animatable(initialValue = 1f) }
    val peakSpec = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
    val settleSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()

    LaunchedEffect(trigger, playMotion) {
        if (playMotion.not() || trigger == firstTrigger) return@LaunchedEffect

        scale.snapTo(targetValue = 1f)
        scale.animateTo(
            targetValue = peakScale,
            animationSpec = peakSpec,
        )
        scale.animateTo(
            targetValue = 1f,
            animationSpec = settleSpec,
        )
    }

    return { scale.value }
}

@Composable
fun rememberPulseScale(
    pulse: NavPulse,
    key: Any,
    peakScale: Float = DEFAULT_PULSE_PEAK_SCALE,
): Float = rememberPulseScale(
    trigger = pulse.countFor(key),
    peakScale = peakScale,
)

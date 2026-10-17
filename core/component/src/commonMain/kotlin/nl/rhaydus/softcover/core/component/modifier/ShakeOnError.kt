package nl.rhaydus.softcover.core.component.modifier

import androidx.compose.animation.core.Animatable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.designsystem.motion.playDecorativeMotion

private const val SHAKE_CYCLES = 3

/** A latch for a shake that must run to the end even when whatever asked for it clears first. */
@Stable
class ShakeTrigger internal constructor() {
    var shaking by mutableStateOf(false)
        private set

    fun shake() {
        shaking = true
    }

    internal fun reset() {
        shaking = false
    }
}

@Composable
fun rememberShakeTrigger(): ShakeTrigger = remember { ShakeTrigger() }

/** Calls [onShakeEnd] when the shake finishes, which is when the caller clears [trigger]. */
@Composable
fun Modifier.shakeOnError(
    trigger: Boolean,
    onShakeEnd: () -> Unit,
): Modifier {
    val offset = remember { Animatable(initialValue = 0f) }
    val amplitudePx = with(LocalDensity.current) { 6.dp.toPx() }
    val currentOnShakeEnd by rememberUpdatedState(newValue = onShakeEnd)
    val haptics = LocalHaptics.current
    val playMotion = playDecorativeMotion()
    val halfPeriodSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()

    LaunchedEffect(trigger) {
        if (trigger.not()) return@LaunchedEffect

        haptics.reject()

        if (playMotion) {
            try {
                repeat(times = SHAKE_CYCLES) {
                    offset.animateTo(
                        targetValue = -amplitudePx,
                        animationSpec = halfPeriodSpec,
                    )

                    offset.animateTo(
                        targetValue = amplitudePx,
                        animationSpec = halfPeriodSpec,
                    )
                }

                offset.animateTo(
                    targetValue = 0f,
                    animationSpec = halfPeriodSpec,
                )
            } finally {
                withContext(NonCancellable) {
                    offset.snapTo(targetValue = 0f)
                }
            }
        }

        currentOnShakeEnd()
    }

    return this.graphicsLayer { translationX = offset.value }
}

@Composable
fun Modifier.shakeOnError(
    trigger: ShakeTrigger,
    onShakeEnd: () -> Unit = {},
): Modifier = shakeOnError(
    trigger = trigger.shaking,
    onShakeEnd = {
        trigger.reset()
        onShakeEnd()
    },
)

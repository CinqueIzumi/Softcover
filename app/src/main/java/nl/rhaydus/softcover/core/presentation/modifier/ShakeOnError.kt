package nl.rhaydus.softcover.core.presentation.modifier

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.presentation.util.rememberHaptics

private val SHAKE_AMPLITUDE = 6.dp
private const val SHAKE_HALF_PERIOD_MS = 80
private const val SHAKE_CYCLES = 3

@Composable
fun Modifier.shakeOnError(
    trigger: Boolean,
    onShakeEnd: () -> Unit,
): Modifier {
    val offset = remember { Animatable(initialValue = 0f) }
    val amplitudePx = with(LocalDensity.current) { SHAKE_AMPLITUDE.toPx() }
    val currentOnShakeEnd by rememberUpdatedState(newValue = onShakeEnd)
    val haptics = rememberHaptics()

    LaunchedEffect(trigger) {
        if (trigger.not()) return@LaunchedEffect

        haptics.reject()

        repeat(times = SHAKE_CYCLES) {
            offset.animateTo(
                targetValue = -amplitudePx,
                animationSpec = tween(durationMillis = SHAKE_HALF_PERIOD_MS),
            )

            offset.animateTo(
                targetValue = amplitudePx,
                animationSpec = tween(durationMillis = SHAKE_HALF_PERIOD_MS),
            )
        }

        offset.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = SHAKE_HALF_PERIOD_MS),
        )

        currentOnShakeEnd()
    }

    return this.graphicsLayer { translationX = offset.value }
}

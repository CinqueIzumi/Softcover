package nl.rhaydus.softcover.core.component.progress

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.component.state.skeletonMask

/**
 * A determinate progress bar. A [fraction] outside 0..1 is clamped when drawn.
 * While [isLoading] the bar is masked, and lifting the mask snaps to [fraction] instead of springing from
 * the stand-in value.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProgressBar(
    fraction: Float,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    val playMotion = playDecorativeMotion()
    val target = fraction.coerceIn(
        minimumValue = 0f,
        maximumValue = 1f,
    )
    val animatedFraction = remember { Animatable(initialValue = target) }
    var wasLoading by remember { mutableStateOf(isLoading) }

    LaunchedEffect(target, isLoading, playMotion) {
        val revealing = wasLoading
        wasLoading = isLoading

        if (playMotion && isLoading.not() && revealing.not()) {
            animatedFraction.animateTo(
                targetValue = target,
                animationSpec = spring(visibilityThreshold = 0.01f),
            )
        } else {
            animatedFraction.snapTo(target)
        }
    }

    LinearWavyProgressIndicator(
        progress = { animatedFraction.value },
        modifier = modifier
            .fillMaxWidth()
            .height(height = 12.dp)
            .skeletonMask(visible = isLoading),
        waveSpeed = 0.dp,
    )
}

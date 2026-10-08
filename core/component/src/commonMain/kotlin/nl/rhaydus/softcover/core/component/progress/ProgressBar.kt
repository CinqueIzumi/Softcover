package nl.rhaydus.softcover.core.component.progress

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.component.state.skeletonMask

/**
 * A determinate progress bar. A [fraction] outside 0..1 is clamped when drawn.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProgressBar(
    fraction: Float,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    val playMotion = playDecorativeMotion()

    val animatedFraction by animateFloatAsState(
        targetValue = fraction.coerceIn(
            minimumValue = 0f,
            maximumValue = 1f,
        ),
        animationSpec = if (playMotion) spring(visibilityThreshold = 0.01f) else snap(),
        label = "progressFraction",
    )

    LinearWavyProgressIndicator(
        progress = { animatedFraction },
        modifier = modifier
            .fillMaxWidth()
            .height(height = 12.dp)
            .skeletonMask(visible = isLoading),
        waveSpeed = 0.dp,
    )
}

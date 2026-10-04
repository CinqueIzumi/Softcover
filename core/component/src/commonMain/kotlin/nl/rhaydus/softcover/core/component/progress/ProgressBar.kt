package nl.rhaydus.softcover.core.component.progress

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.state.skeletonMask

@Composable
fun ProgressBar(
    model: ProgressBarUiModel,
    modifier: Modifier = Modifier,
) {
    ProgressBarBody(
        fraction = model.fraction,
        modifier = modifier,
        isLoading = model.isLoading,
    )
}

/**
 * Draws the bar from a bare fraction, for callers whose fraction comes from input state the component owns.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ProgressBarBody(
    fraction: Float,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        label = "progressFraction",
    )

    LinearWavyProgressIndicator(
        progress = { animatedFraction },
        modifier = modifier
            .fillMaxWidth()
            .height(height = 12.dp)
            .skeletonMask(visible = isLoading),
    )
}

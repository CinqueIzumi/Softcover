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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun EditorialProgressIndicator(fraction: Float) {
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        label = "progressFraction",
    )

    LinearWavyProgressIndicator(
        progress = { animatedFraction },
        modifier = Modifier
            .fillMaxWidth()
            .height(height = 12.dp),
    )
}

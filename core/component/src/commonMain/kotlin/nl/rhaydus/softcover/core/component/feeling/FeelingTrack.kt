package nl.rhaydus.softcover.core.component.feeling

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.motion.playDecorativeMotion

@Composable
internal fun FeelingTrack(
    strength: Int?,
    modifier: Modifier = Modifier,
) {
    val playMotion = playDecorativeMotion()
    val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()

    val fill by animateFloatAsState(
        targetValue = (strength ?: 0) / 100f,
        animationSpec = if (playMotion) spatialSpec else snap(),
        label = "FeelingTrackFill",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fill)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.primary),
        )
    }
}

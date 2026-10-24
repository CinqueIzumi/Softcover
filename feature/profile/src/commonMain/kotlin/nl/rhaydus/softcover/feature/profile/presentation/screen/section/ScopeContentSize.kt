package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.animation.animateContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.motion.playDecorativeMotion

/** Eases the height change when a scope swaps a section between its chart and its empty line. Reduced motion snaps. */
@Composable
internal fun Modifier.animateScopeContentSize(): Modifier = if (playDecorativeMotion()) {
    animateContentSize(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec())
} else {
    this
}

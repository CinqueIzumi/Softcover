package nl.rhaydus.softcover.core.component.feeling

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
internal fun FeelingReveal(
    visible: Boolean,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()) +
            fadeIn(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()),
        exit = shrinkVertically(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()) +
            fadeOut(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()),
        content = content,
    )
}

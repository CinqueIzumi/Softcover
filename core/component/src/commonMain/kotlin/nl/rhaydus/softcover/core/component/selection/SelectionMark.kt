package nl.rhaydus.softcover.core.component.selection

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * A circular selection mark. Inert and silent: the caller makes the surface around it tappable and owns the
 * selection semantics.
 */
@Composable
fun SelectionMark(
    selected: Boolean,
    modifier: Modifier = Modifier,
    backdrop: SelectionMarkBackdrop = SelectionMarkBackdrop.Image,
) {
    val scheme = MaterialTheme.colorScheme
    val motionScheme = MaterialTheme.motionScheme
    val container by animateColorAsState(
        targetValue = when {
            selected -> scheme.primary
            backdrop == SelectionMarkBackdrop.Image -> scheme.surface.copy(alpha = 0.72f)
            else -> scheme.surfaceContainerHigh
        },
        animationSpec = motionScheme.slowEffectsSpec<Color>(),
    )

    Surface(
        modifier = modifier.size(24.dp),
        color = container,
        shape = CircleShape,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            SelectionCheck(
                selected = selected,
                size = 16.dp,
                tint = scheme.onPrimary,
            )
        }
    }
}

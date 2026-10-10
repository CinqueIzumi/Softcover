package nl.rhaydus.softcover.core.component.selection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource

/**
 * A circular selection mark. Inert: the caller makes the surface around it tappable and owns the
 * selection.
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
        modifier = modifier
            .size(24.dp)
            .semantics {
                this.selected = selected
                stateDescription = if (selected) "Selected" else "Not selected"
            },
        color = container,
        shape = CircleShape,
    ) {
        AnimatedVisibility(
            visible = selected,
            enter = scaleIn(animationSpec = motionScheme.defaultSpatialSpec()) +
                fadeIn(animationSpec = motionScheme.fastEffectsSpec()),
            exit = scaleOut(animationSpec = motionScheme.fastEffectsSpec()) +
                fadeOut(animationSpec = motionScheme.fastEffectsSpec()),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                val icon = drawableIconResource(
                    icon = SoftcoverIcon.Check,
                    contentDescription = "",
                )

                Icon(
                    painter = icon.getIconPainter(),
                    contentDescription = null,
                    tint = scheme.onPrimary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

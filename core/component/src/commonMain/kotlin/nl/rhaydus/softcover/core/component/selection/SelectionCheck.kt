package nl.rhaydus.softcover.core.component.selection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource

/** An accent check that keeps its space when unselected. Inert: the caller owns the selection. */
@Composable
fun SelectionCheck(
    selected: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
) {
    val motionScheme = MaterialTheme.motionScheme

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = selected,
            enter = scaleIn(animationSpec = motionScheme.defaultSpatialSpec()) +
                fadeIn(animationSpec = motionScheme.fastEffectsSpec()),
            exit = scaleOut(animationSpec = motionScheme.fastEffectsSpec()) +
                fadeOut(animationSpec = motionScheme.fastEffectsSpec()),
        ) {
            val icon = drawableIconResource(
                icon = SoftcoverIcon.Check,
                contentDescription = "",
            )

            Icon(
                painter = icon.getIconPainter(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(size),
            )
        }
    }
}

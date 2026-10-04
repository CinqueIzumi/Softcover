package nl.rhaydus.softcover.core.component.selection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource

/**
 * A circular selection mark: filled with a check mark when selected, an empty ring of
 * `model.backdrop`'s unselected fill otherwise. Inert: a mark is read, never tapped, so it takes no
 * event lambda (§ 7.2 R1 — the caller hoists selection onto the tappable surface around it).
 */
@Composable
fun SelectionMark(
    model: SelectionMarkUiModel,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val container = when {
        model.selected -> scheme.primary
        model.backdrop == SelectionMarkBackdrop.Image -> scheme.surface.copy(alpha = 0.72f)
        else -> scheme.surfaceContainerHigh
    }

    Surface(
        modifier = modifier
            .size(24.dp)
            .semantics {
                selected = model.selected
                stateDescription = if (model.selected) "Selected" else "Not selected"
            },
        color = container,
        shape = CircleShape,
    ) {
        if (model.selected) {
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

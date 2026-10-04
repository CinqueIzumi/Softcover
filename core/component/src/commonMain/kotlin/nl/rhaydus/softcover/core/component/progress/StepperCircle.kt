package nl.rhaydus.softcover.core.component.progress

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource

/**
 * A 44dp circle button flanking a hero number field: `surfaceContainerHigh` fill, primary [icon].
 * A tap calls [onClick], which sends a step event to the host; the circle holds no value of its own.
 */
@Composable
internal fun StepperCircle(
    icon: SoftcoverIcon,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .size(44.dp)
            // The visible glyph would otherwise merge into the announced label alongside
            // the content description; clear it and re-declare only what a screen reader should
            // say — the description plus the button role/action Surface's own onClick provides.
            .clearAndSetSemantics {
                role = Role.Button
                this.contentDescription = contentDescription
                onClick(label = null) {
                    onClick()
                    true
                }
            },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.primary,
        onClick = onClick,
    ) {
        Box(contentAlignment = Alignment.Center) {
            val resource = drawableIconResource(
                icon = icon,
                contentDescription = "",
            )

            Icon(
                painter = resource.getIconPainter(),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

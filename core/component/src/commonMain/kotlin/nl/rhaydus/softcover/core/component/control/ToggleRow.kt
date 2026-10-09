package nl.rhaydus.softcover.core.component.control

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * A labelled switch whose whole width is the tap target: the row is the single `Switch` semantics node and
 * its switch is display-only. A press washes the row in `surfaceContainer`, under the padding of
 * [ToggleRowUiModel.variant], which sits inside the tap target.
 */
@Composable
fun <K> ToggleRow(
    model: ToggleRowUiModel<K>,
    onEvent: (ToggleEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val playMotion = playDecorativeMotion()
    val wash by animateColorAsState(
        targetValue = if (isPressed && playMotion) {
            MaterialTheme.colorScheme.surfaceContainer
        } else {
            Color.Transparent
        },
        label = "ToggleRowWash",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .pointerHandCursor()
            .toggleable(
                value = model.checked,
                interactionSource = interactionSource,
                indication = null,
                enabled = model.enabled,
                role = Role.Switch,
                onValueChange = { checked ->
                    onEvent(ToggleEvent.Toggled(checked = checked))
                },
            )
            .background(wash)
            .then(
                if (model.variant == ToggleRowVariant.Inset) {
                    Modifier.padding(
                        horizontal = Spacing.xxs,
                        vertical = Spacing.md,
                    )
                } else {
                    Modifier
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            SoftcoverText(
                text = model.title,
                style = MaterialTheme.editorialTypography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (model.gloss != null) {
                Spacer(modifier = Modifier.height(Spacing.xxs))

                SoftcoverText(
                    text = model.gloss,
                    style = MaterialTheme.editorialTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.width(Spacing.md))

        Switch(
            checked = model.checked,
            onCheckedChange = null,
            enabled = model.enabled,
        )
    }
}

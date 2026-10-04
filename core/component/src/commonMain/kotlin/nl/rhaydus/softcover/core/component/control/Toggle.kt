package nl.rhaydus.softcover.core.component.control

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.DesktopTooltip
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource

/** A two-state control reporting through one [ToggleEvent] lambda; the caller's [modifier] places it. */
@Composable
fun Toggle(
    model: ToggleUiModel,
    onEvent: (ToggleEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (val variant = model.variant) {
        is ToggleVariant.Icon -> when (variant.style) {
            ToggleIconStyle.Filled -> FilledIconToggle(
                model = model,
                variant = variant,
                onEvent = onEvent,
                modifier = modifier,
            )

            ToggleIconStyle.Bare -> BareIconToggle(
                model = model,
                variant = variant,
                onEvent = onEvent,
                modifier = modifier,
            )
        }

        ToggleVariant.Switch -> Switch(
            checked = model.checked,
            onCheckedChange = { checked ->
                onEvent(
                    ToggleEvent.Toggled(
                        key = model.key,
                        checked = checked,
                    ),
                )
            },
            enabled = model.enabled,
            modifier = modifier.semantics { contentDescription = model.description },
        )

        ToggleVariant.DisplaySwitch -> Switch(
            checked = model.checked,
            onCheckedChange = null,
            enabled = model.enabled,
            modifier = modifier,
        )
    }
}

@Composable
private fun FilledIconToggle(
    model: ToggleUiModel,
    variant: ToggleVariant.Icon,
    onEvent: (ToggleEvent) -> Unit,
    modifier: Modifier,
) {
    Surface(
        checked = model.checked,
        onCheckedChange = { checked ->
            onEvent(
                ToggleEvent.Toggled(
                    key = model.key,
                    checked = checked,
                ),
            )
        },
        enabled = model.enabled,
        modifier = modifier
            .size(44.dp)
            .pointerHandCursor()
            .semantics { role = Role.Switch },
        shape = RoundedCornerShape(12.dp),
        color = if (model.checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = if (model.checked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
    ) {
        Box(contentAlignment = Alignment.Center) {
            val icon = drawableIconResource(
                icon = if (model.checked) variant.on else variant.off,
                contentDescription = model.description,
            )

            Icon(
                painter = icon.getIconPainter(),
                contentDescription = icon.contentDescription,
            )
        }
    }
}

@Composable
private fun BareIconToggle(
    model: ToggleUiModel,
    variant: ToggleVariant.Icon,
    onEvent: (ToggleEvent) -> Unit,
    modifier: Modifier,
) {
    val icon = drawableIconResource(
        icon = if (model.checked) variant.on else variant.off,
        contentDescription = model.description,
    )

    DesktopTooltip(text = model.description) {
        Box(
            modifier = modifier
                .size(38.dp)
                .clip(CircleShape)
                .alpha(if (model.enabled) 1f else 0.4f)
                .pointerHandCursor()
                .pressScaleClickable(
                    onClick = {
                        if (model.enabled) {
                            onEvent(
                                ToggleEvent.Toggled(
                                    key = model.key,
                                    checked = model.checked.not(),
                                ),
                            )
                        }
                    },
                )
                .semantics {
                    role = Role.Switch
                    toggleableState = ToggleableState(model.checked)
                    if (model.enabled.not()) disabled()
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = icon.getIconPainter(),
                contentDescription = icon.contentDescription,
                tint = if (model.checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

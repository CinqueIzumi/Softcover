package nl.rhaydus.softcover.core.component.control

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import nl.rhaydus.designsystem.component.DesktopTooltip
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScale
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.component.DISABLED_ALPHA
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource

/** A two-state control reporting through one [ToggleEvent] lambda; the caller's [modifier] places it. */
@Composable
fun Toggle(
    model: ToggleUiModel,
    onEvent: (ToggleEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (val variant = model.variant) {
        is ToggleVariant.Icon -> IconToggle(
            model = model,
            variant = variant,
            onEvent = onEvent,
            modifier = modifier,
        )

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
    }
}

@Composable
private fun IconToggle(
    model: ToggleUiModel,
    variant: ToggleVariant.Icon,
    onEvent: (ToggleEvent) -> Unit,
    modifier: Modifier,
) {
    val style = variant.style.style(
        scheme = MaterialTheme.colorScheme,
        checked = model.checked,
    )
    val playMotion = playDecorativeMotion()
    val colorSpec = if (playMotion) tween<Color>(durationMillis = 220) else snap()
    val container by animateColorAsState(
        targetValue = style.container,
        animationSpec = colorSpec,
        label = "IconToggleContainer",
    )
    val tint by animateColorAsState(
        targetValue = style.tint,
        animationSpec = colorSpec,
        label = "IconToggleTint",
    )
    val interactionSource = remember { MutableInteractionSource() }
    val icon = drawableIconResource(
        icon = if (model.checked) variant.on else variant.off,
        contentDescription = model.description,
    )

    val tile: @Composable () -> Unit = {
        Box(
            modifier = modifier
                .minimumInteractiveComponentSize()
                .alpha(if (model.enabled) 1f else DISABLED_ALPHA)
                .size(style.size)
                .pressScale(interactionSource)
                .clip(style.shape)
                .background(container)
                .pointerHandCursor()
                .toggleable(
                    value = model.checked,
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = model.enabled,
                    role = Role.Switch,
                    onValueChange = { checked ->
                        onEvent(
                            ToggleEvent.Toggled(
                                key = model.key,
                                checked = checked,
                            ),
                        )
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = icon.getIconPainter(),
                contentDescription = icon.contentDescription,
                tint = tint,
                modifier = Modifier.size(style.iconSize),
            )
        }
    }

    if (style.hasTooltip) {
        DesktopTooltip(text = model.description) {
            tile()
        }
    } else {
        tile()
    }
}

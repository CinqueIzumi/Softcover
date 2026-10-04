package nl.rhaydus.softcover.core.component.control

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.DesktopTooltip
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScale
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.component.DISABLED_ALPHA
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource

/** A two-state glyph control reporting through one [ToggleEvent] lambda; the caller's [modifier] places it. */
@Composable
fun Toggle(
    model: ToggleUiModel,
    onEvent: (ToggleEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val filled = model.variant == ToggleVariant.Filled
    val size = if (filled) 44.dp else 38.dp
    val iconSize = if (filled) 24.dp else 20.dp
    val shape = if (filled) RoundedCornerShape(12.dp) else CircleShape
    val targetContainer = when {
        filled.not() -> Color.Transparent
        model.checked -> scheme.primary
        else -> scheme.surfaceContainerHigh
    }
    val targetTint = when {
        filled && model.checked -> scheme.onPrimary
        filled -> scheme.primary
        model.checked -> scheme.primary
        else -> scheme.outline
    }
    val playMotion = playDecorativeMotion()
    val colorSpec = if (playMotion) tween<Color>(durationMillis = 220) else snap()
    val container by animateColorAsState(
        targetValue = targetContainer,
        animationSpec = colorSpec,
        label = "IconToggleContainer",
    )
    val tint by animateColorAsState(
        targetValue = targetTint,
        animationSpec = colorSpec,
        label = "IconToggleTint",
    )
    val interactionSource = remember { MutableInteractionSource() }
    val icon = drawableIconResource(
        icon = if (model.checked) model.on else model.off,
        contentDescription = model.description,
    )

    DesktopTooltip(text = model.description) {
        Box(
            modifier = modifier
                .minimumInteractiveComponentSize()
                .alpha(if (model.enabled) 1f else DISABLED_ALPHA)
                .size(size)
                .pressScale(interactionSource)
                .clip(shape)
                .background(container)
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
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = icon.getIconPainter(),
                contentDescription = icon.contentDescription,
                tint = tint,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}

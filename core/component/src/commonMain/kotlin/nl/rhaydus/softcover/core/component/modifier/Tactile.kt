package nl.rhaydus.softcover.core.component.modifier

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.designsystem.modifier.hoverHighlight
import nl.rhaydus.designsystem.modifier.pointerHandCursor

private const val PRESS_SCALE_TARGET = 0.97f

/**
 * The app's one interaction modifier: ripple-less press scale, desktop hover wash, hand cursor and click
 * semantics. A null [hoverShape] paints no hover wash. Unlike [tactileSelectable] and [tactileToggleable]
 * it plays no haptic; [onClick] and [onLongClick] fire their own.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.tactileClickable(
    onClick: () -> Unit,
    role: Role? = Role.Button,
    enabled: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    hoverShape: Shape? = RectangleShape,
    interactionSource: MutableInteractionSource? = null,
): Modifier {
    val source = interactionSource ?: remember { MutableInteractionSource() }

    return this
        .tactileFeedback(
            interactionSource = source,
            enabled = enabled,
            hoverShape = hoverShape,
        )
        .let { base ->
            if (onLongClick == null) {
                base.clickable(
                    interactionSource = source,
                    indication = null,
                    enabled = enabled,
                    role = role,
                    onClick = onClick,
                )
            } else {
                base.combinedClickable(
                    interactionSource = source,
                    indication = null,
                    enabled = enabled,
                    role = role,
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
            }
        }
}

@Composable
fun Modifier.tactileSelectable(
    selected: Boolean,
    onClick: () -> Unit,
    role: Role? = null,
    enabled: Boolean = true,
    hoverShape: Shape? = RectangleShape,
    interactionSource: MutableInteractionSource? = null,
): Modifier {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val haptics = LocalHaptics.current

    return this
        .tactileFeedback(
            interactionSource = source,
            enabled = enabled,
            hoverShape = hoverShape,
        )
        .selectable(
            selected = selected,
            interactionSource = source,
            indication = null,
            enabled = enabled,
            role = role,
            onClick = {
                if (selected.not()) haptics.select()
                onClick()
            },
        )
}

@Composable
fun Modifier.tactileToggleable(
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    role: Role? = null,
    enabled: Boolean = true,
    hoverShape: Shape? = RectangleShape,
    interactionSource: MutableInteractionSource? = null,
): Modifier {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val haptics = LocalHaptics.current

    return this
        .tactileFeedback(
            interactionSource = source,
            enabled = enabled,
            hoverShape = hoverShape,
        )
        .toggleable(
            value = value,
            interactionSource = source,
            indication = null,
            enabled = enabled,
            role = role,
            onValueChange = { next ->
                haptics.select()
                onValueChange(next)
            },
        )
}

@Composable
private fun Modifier.tactileFeedback(
    interactionSource: MutableInteractionSource,
    enabled: Boolean,
    hoverShape: Shape?,
): Modifier = if (enabled) {
    this
        .pointerHandCursor()
        .tactilePressScale(interactionSource)
        .let { base ->
            if (hoverShape == null) {
                base
            } else {
                base.hoverHighlight(
                    interactionSource = interactionSource,
                    shape = hoverShape,
                )
            }
        }
} else {
    this
}

/**
 * Fork of the foundation's `pressScale` driven by the motion scheme's fast spatial spec. It carries no
 * `playDecorativeMotion` gate: the reduced-motion scheme already snaps.
 */
@Composable
internal fun Modifier.tactilePressScale(interactionSource: MutableInteractionSource): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) PRESS_SCALE_TARGET else 1f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "tactilePressScale",
    )

    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

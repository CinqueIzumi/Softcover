package nl.rhaydus.softcover.core.component.modifier

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import nl.rhaydus.designsystem.modifier.hoverHighlight
import nl.rhaydus.designsystem.modifier.pressScale

/**
 * Ripple-less clickable with press-scale feedback and a desktop hover wash clipped to [hoverShape].
 */
@Composable
fun Modifier.pressScaleClickable(
    onClick: () -> Unit,
    hoverShape: Shape = RectangleShape,
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }

    return this
        .pressScale(interactionSource)
        .hoverHighlight(
            interactionSource = interactionSource,
            shape = hoverShape,
        )
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
        )
}

/**
 * [pressScaleClickable] with an optional long-press handler. The handler must fire its own haptic.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.pressScaleCombinedClickable(
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    hoverShape: Shape = RectangleShape,
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }

    return this
        .pressScale(interactionSource)
        .hoverHighlight(
            interactionSource = interactionSource,
            shape = hoverShape,
        )
        .combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
            onLongClick = onLongClick,
        )
}

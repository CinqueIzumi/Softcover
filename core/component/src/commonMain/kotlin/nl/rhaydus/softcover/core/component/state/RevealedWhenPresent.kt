package nl.rhaydus.softcover.core.component.state

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Reveals [content] vertically while [value] is non-null, for rows that arrive on their own timer
 * and may never arrive at all, so they take no height until they do.
 *
 * [content] renders the last non-null value, so an exit animation keeps drawing the row it shrinks
 * instead of collapsing the frame [value] turns null. The row grows down from its top edge, so rows above it
stay put. Both transitions are no-ops when decorative
 * motion is off.
 */
@Composable
fun <T : Any> RevealedWhenPresent(
    value: T?,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    var lastValue by remember { mutableStateOf(value) }

    if (value != null) {
        lastValue = value
    }

    AnimatedVisibility(
        visible = value != null,
        modifier = modifier,
        enter = expandVertically(
            animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
            expandFrom = Alignment.Top,
        ) +
            fadeIn(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()),
        exit = shrinkVertically(
            animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
            shrinkTowards = Alignment.Top,
        ) +
            fadeOut(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()),
    ) {
        lastValue?.let { shown ->
            content(shown)
        }
    }
}

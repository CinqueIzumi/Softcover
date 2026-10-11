package nl.rhaydus.softcover.core.component.progress

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.designsystem.modifier.pressScale
import nl.rhaydus.softcover.core.component.DISABLED_ALPHA
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource

private const val HOLD_DELAY_MS = 400L
private const val SLOWEST_REPEAT_MS = 150L
private const val FASTEST_REPEAT_MS = 40L
private const val RAMP_MS = 2000L

/**
 * A 44dp circle button flanking a hero number field: `surfaceContainerHigh` fill, primary [icon].
 * Pressing calls [onClick] with a tickle haptic and holding repeats it, faster the longer it is held;
 * the circle holds no value of its own. While not [enabled] the glyph dims and a running repeat stops.
 */
@Composable
internal fun StepperCircle(
    icon: SoftcoverIcon,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val haptics = LocalHaptics.current
    val currentOnClick by rememberUpdatedState(onClick)
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .size(44.dp)
            .pressScale(interactionSource)
            // The visible glyph would otherwise merge into the announced label alongside
            // the content description; clear it and re-declare only what a screen reader should
            // say, the description plus the button role and a single-step click action.
            .clearAndSetSemantics {
                role = Role.Button
                this.contentDescription = contentDescription
                if (enabled) {
                    onClick(label = null) {
                        currentOnClick()
                        haptics.tickle()
                        true
                    }
                } else {
                    disabled()
                }
            },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = if (enabled) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.primary.copy(alpha = DISABLED_ALPHA)
        },
    ) {
        Box(
            modifier = Modifier
                .indication(
                    interactionSource = interactionSource,
                    indication = ripple(),
                )
                .repeatingPress(
                    interactionSource = interactionSource,
                    enabled = enabled,
                    onStep = {
                        currentOnClick()
                        haptics.tickle()
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
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

/**
 * Steps once on pointer down, then after a hold delay repeats [onStep] at an interval that shortens
 * while held. Release, cancel, leaving the bounds, or [enabled] turning false ends the press.
 */
private fun Modifier.repeatingPress(
    interactionSource: MutableInteractionSource,
    enabled: Boolean,
    onStep: () -> Unit,
): Modifier = pointerInput(enabled, interactionSource) {
    if (enabled.not()) return@pointerInput

    awaitEachGesture {
        val down = awaitFirstDown()
        down.consume()
        val press = PressInteraction.Press(down.position)
        interactionSource.tryEmit(press)
        var finished = false

        try {
            onStep()
            var wait = HOLD_DELAY_MS
            var heldMs = 0L

            while (finished.not()) {
                // true: released, false: cancelled or left the bounds, null: still held
                val released = withTimeoutOrNull(wait) {
                    waitForUpOrCancellation() != null
                }

                when (released) {
                    true -> {
                        interactionSource.tryEmit(PressInteraction.Release(press))
                        finished = true
                    }

                    false -> {
                        interactionSource.tryEmit(PressInteraction.Cancel(press))
                        finished = true
                    }

                    null -> {
                        onStep()
                        heldMs += wait
                        wait = repeatIntervalMs(heldMs - HOLD_DELAY_MS)
                    }
                }
            }
        } finally {
            if (finished.not()) {
                interactionSource.tryEmit(PressInteraction.Cancel(press))
            }
        }
    }
}

private fun repeatIntervalMs(heldMs: Long): Long {
    val progress = (heldMs.coerceAtLeast(0L).toFloat() / RAMP_MS).coerceAtMost(1f)

    return (SLOWEST_REPEAT_MS + (FASTEST_REPEAT_MS - SLOWEST_REPEAT_MS) * progress).toLong()
}

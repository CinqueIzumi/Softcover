package nl.rhaydus.softcover.core.designsystem.presentation.modifier

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

/**
 * Dismisses an in-app desktop overlay when the user presses **Esc** — the baseline desktop
 * expectation for any surface that sits over the page (full-screen reading, the cover viewer, a
 * selection mode). jvm-only: touch platforms have no keyboard, and the desktop modal panel
 * ([AdaptiveModalSheet]'s expanded form) is a `Dialog`, which already maps `dismissOnBackPress` to Esc
 * — so this is only for the in-app surfaces that are *not* a `Dialog`/`Popup`.
 *
 * While [enabled], the node takes focus once so key events reach it even when nothing else is focused;
 * because the handler runs in the **preview** (tunnelling) phase, Esc is caught even when a descendant
 * text field holds focus (Esc is not a text-entry key, so nothing else consumes it). When not
 * [enabled] the modifier is a no-op and never grabs focus, so it can be gated on a transient mode
 * (e.g. bulk-select) without disturbing normal focus.
 */
@Composable
fun Modifier.dismissOnEscape(
    enabled: Boolean = true,
    onDismiss: () -> Unit,
): Modifier {
    // Always composed (never behind the early `enabled` branch) so the remember slot count stays
    // stable when `enabled` toggles between recompositions — otherwise flipping a transient mode
    // (e.g. bulk-select) would corrupt the composition.
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(enabled) {
        // The node may not be attached yet on the first frame; the request is a no-op until it is.
        if (enabled) runCatching { focusRequester.requestFocus() }
    }

    if (enabled.not()) return this

    return this
        .focusRequester(focusRequester)
        .focusTarget()
        .onPreviewKeyEvent { event ->
            if (event.type == KeyEventType.KeyDown && event.key == Key.Escape) {
                onDismiss()
                true
            } else {
                false
            }
        }
}

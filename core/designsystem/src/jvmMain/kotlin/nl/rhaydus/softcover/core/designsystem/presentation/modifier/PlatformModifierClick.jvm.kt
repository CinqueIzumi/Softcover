package nl.rhaydus.softcover.core.designsystem.presentation.modifier

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isMetaPressed
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.input.pointer.pointerInput

// Reads the keyboard modifiers on the primary press in the Initial pass and consumes it only when a
// modifier is held, so the inner `combinedClickable` keeps handling plain clicks while ctrl/cmd- and
// shift-clicks are intercepted here. Shift takes precedence over ctrl/cmd when both are held.
@Composable
actual fun Modifier.platformModifierClick(
    onCtrlClick: () -> Unit,
    onShiftClick: () -> Unit,
): Modifier = this.pointerInput(onCtrlClick, onShiftClick) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)

            if (event.buttons.isPrimaryPressed.not()) continue

            val down = event.changes.firstOrNull { it.changedToDownIgnoreConsumed() } ?: continue

            val modifiers = event.keyboardModifiers
            val ctrlOrMeta = modifiers.isCtrlPressed || modifiers.isMetaPressed
            val shift = modifiers.isShiftPressed

            when {
                shift -> {
                    down.consume()

                    onShiftClick()
                }

                ctrlOrMeta -> {
                    down.consume()

                    onCtrlClick()
                }
            }
        }
    }
}

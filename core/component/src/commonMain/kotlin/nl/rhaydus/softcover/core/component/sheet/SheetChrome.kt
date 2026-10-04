package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import nl.rhaydus.designsystem.component.LocalModalSheetDismiss

/**
 * What the scaffold's tappable regions need to know about busy and dismissing state. [close] latches
 * [dismissStarted] and hides the sheet once, so a second tap during the animated hide cannot emit a second
 * event.
 */
@Stable
internal class SheetChrome(
    val busy: Boolean,
    val canDismiss: Boolean,
    val dismissStarted: Boolean,
    val close: () -> Unit,
) {
    /** False once a dismiss has started; the chrome stops reacting to taps. */
    val unlocked: Boolean get() = dismissStarted.not()

    /** Starts the animated hide unless the sheet is not dismissible. */
    fun dismiss() {
        if (canDismiss) {
            close()
        }
    }
}

@Composable
internal fun rememberSheetChrome(model: SheetScaffoldUiModel): SheetChrome {
    val hide by rememberUpdatedState(LocalModalSheetDismiss.current)
    val started = remember { mutableStateOf(false) }

    return SheetChrome(
        busy = model.busy,
        canDismiss = model.dismissible && model.busy.not(),
        dismissStarted = started.value,
        close = {
            if (started.value.not()) {
                started.value = true
                hide()
            }
        },
    )
}

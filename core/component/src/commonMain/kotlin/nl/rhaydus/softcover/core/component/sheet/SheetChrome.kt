package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import nl.rhaydus.designsystem.component.LocalModalSheetDismiss

/**
 * What the scaffold's tappable regions need to know about busy and dismissing state. [close] latches
 * [dismissStarted] and hides the sheet once, so a second tap during the animated hide cannot emit a second
 * event. [dismissStarted] is also true once the sheet itself starts closing by scrim, back or swipe.
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
        if (canDismiss && unlocked) {
            close()
        }
    }
}

@Composable
internal fun rememberSheetChrome(
    model: SheetScaffoldUiModel,
    dismissStarted: MutableState<Boolean>,
): SheetChrome {
    val hide by rememberUpdatedState(LocalModalSheetDismiss.current)
    val sheetClosing = LocalSheetClosing.current

    return SheetChrome(
        busy = model.busy,
        canDismiss = model.dismissible && model.busy.not(),
        dismissStarted = dismissStarted.value || sheetClosing,
        close = {
            if (dismissStarted.value.not()) {
                dismissStarted.value = true
                hide()
            }
        },
    )
}

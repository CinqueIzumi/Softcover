package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import nl.rhaydus.designsystem.component.LocalModalSheetDismiss

/** What the scaffold's tappable regions need to know about busy and dismissing state. */
@Stable
internal class SheetChrome(
    val busy: Boolean,
    val busyDescription: String,
    val canDismiss: Boolean,
    val dismissal: SheetDismissal,
) {
    /** False once a dismiss has started; the chrome stops reacting to taps. */
    val unlocked: Boolean get() = dismissal.started.not()

    /** Starts the animated hide unless the sheet is not dismissible. */
    fun dismiss() {
        if (canDismiss) {
            dismissal.start()
        }
    }
}

@Composable
internal fun rememberSheetChrome(
    busy: Boolean = false,
    busyDescription: String = "",
    dismissible: Boolean = true,
): SheetChrome {
    val hide by rememberUpdatedState(LocalModalSheetDismiss.current)
    val dismissal = remember { SheetDismissal(hide = { hide() }) }

    return SheetChrome(
        busy = busy,
        busyDescription = busyDescription,
        canDismiss = dismissible && busy.not(),
        dismissal = dismissal,
    )
}

@Composable
internal fun rememberSheetChrome(model: SheetScaffoldUiModel): SheetChrome = rememberSheetChrome(
    busy = model.busy,
    busyDescription = model.busyDescription,
    dismissible = model.dismissible,
)

package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.runtime.Immutable

/**
 * [loading] disables the button and shows a progress indicator over it. [dismissesSheet] makes the
 * scaffold run the sheet's animated dismiss after emitting the click, so the click's handler must not
 * close the sheet itself; the scaffold then reports [SheetScaffoldEvent.Dismissed].
 */
@Immutable
data class SheetFooterButton(
    val label: String,
    val enabled: Boolean = true,
    val loading: Boolean = false,
    val dismissesSheet: Boolean = false,
)

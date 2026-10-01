package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.runtime.Immutable

/**
 * [dismissesSheet] makes the scaffold run the sheet's animated dismiss after emitting the click, so the
 * click's handler must not close the sheet itself; the scaffold then reports
 * [SheetScaffoldEvent.Dismissed]. A sheet that is not dismissible ignores it.
 */
@Immutable
data class SheetFooterButton(
    val label: String,
    val enabled: Boolean = true,
    val dismissesSheet: Boolean = false,
)

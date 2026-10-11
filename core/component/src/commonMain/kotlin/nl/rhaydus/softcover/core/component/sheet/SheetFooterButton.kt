package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/**
 * [running] shows a spinner on this button while its work is in flight and disables the whole footer.
 * [dismissesSheet] makes the scaffold run the sheet's animated dismiss after emitting the click, so the
 * click's handler must not close the sheet itself; the scaffold then reports
 * [SheetScaffoldEvent.Dismissed]. A sheet that is not dismissible ignores it.
 */
@Immutable
data class SheetFooterButton(
    val label: String,
    val enabled: Boolean = true,
    val dismissesSheet: Boolean = false,
    val running: Boolean = false,
    val emphasis: SheetFooterEmphasis = SheetFooterEmphasis.Default,
    val icon: SoftcoverIcon? = null,
)

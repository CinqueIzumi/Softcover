package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.runtime.Immutable

/** One footer button. [loading] disables the button and shows a progress indicator over it. */
@Immutable
data class SheetFooterButton(
    val label: String,
    val enabled: Boolean = true,
    val loading: Boolean = false,
)

package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

/**
 * The group runs the sheet's animated dismiss after emitting [SheetRowEvent.Selected], so the host's
 * handler must not close the sheet itself.
 */
@Immutable
data class SheetRowGroupUiModel<K>(
    val rows: ImmutableList<SheetRowUiModel<K>>,
)

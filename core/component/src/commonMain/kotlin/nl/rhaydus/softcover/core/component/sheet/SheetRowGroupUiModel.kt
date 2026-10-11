package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

/**
 * A row selection runs the sheet's animated dismiss after [SheetRowEvent.Selected], so the host's
 * handler must not close the sheet itself. When [dismissesSheet] is false the group leaves the
 * sheet open and the host closes it through [SheetSession.requestClose], for a host that
 * must apply the selection before the hide starts.
 */
@Immutable
data class SheetRowGroupUiModel<K>(
    val rows: ImmutableList<SheetRowUiModel<K>>,
    val dismissesSheet: Boolean = true,
)

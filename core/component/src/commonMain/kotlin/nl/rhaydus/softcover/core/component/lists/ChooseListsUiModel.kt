package nl.rhaydus.softcover.core.component.lists

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.component.state.EmptyStateUiModel
import nl.rhaydus.softcover.core.component.state.EmptyStateVariant

/**
 * Everything `ChooseListsBottomSheet` renders: the [scaffold] chrome naming who is being shelved, and
 * the reader's custom lists with each one's membership relative to that selection.
 *
 * Which lists to offer is the host's decision, not the mapping's — [rows] is exactly what the sheet
 * shows, in the order it shows them.
 */
@Immutable
data class ChooseListsUiModel(
    val rows: ImmutableList<ChooseListsRowUiModel>,
    val scaffold: SheetScaffoldUiModel,
    val emptyState: EmptyStateUiModel = EmptyStateUiModel(
        title = "No custom lists yet",
        body = "Create your first list below.",
        variant = EmptyStateVariant.Inline,
    ),
)

package nl.rhaydus.softcover.core.component.lists

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.chip.ChipUiModel

/**
 * One ruled-index row in the choose-lists sheet.
 *
 * [caption] arrives already written — "12 BOOKS · 3 OF 5 HERE" — because composing it needs the
 * selection size and how much of it the list already holds, which is the feature's arithmetic, not
 * the row's (R4). [membershipChip] is rendered with
 * [nl.rhaydus.softcover.core.component.chip.ChipInteraction.Inert] — the whole row is the tap
 * target, and it commits [membership] toward the larger end ([ListMembership.NONE] and
 * [ListMembership.PARTIAL] both move to [ListMembership.ALL], [ListMembership.ALL] clears to
 * [ListMembership.NONE]).
 *
 * [membership] stays on the model even though [membershipChip] already carries the matching
 * [nl.rhaydus.softcover.core.component.chip.ChipVariant] — the row's leading bookmark glyph and the
 * caption's ink both key off it too.
 */
@Immutable
data class ChooseListsRowUiModel(
    val listId: Int,
    val name: String,
    val caption: String,
    val membershipChip: ChipUiModel,
    val membership: ListMembership,
    /** A mutation is in flight: the row is tap-disabled and shows the house wavy indicator, never a circular spinner. */
    val isPending: Boolean,
)

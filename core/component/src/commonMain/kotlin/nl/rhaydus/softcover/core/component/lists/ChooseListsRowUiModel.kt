package nl.rhaydus.softcover.core.component.lists

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.chip.ChipUiModel

/**
 * One ruled-index row in the choose-lists sheet.
 *
 * [caption] arrives already written — "12 BOOKS · 3 OF 5 HERE" — because composing it needs the
 * selection size and how much of it the list already holds, which is the feature's arithmetic, not
 * the row's. The chip is non-interactive; the whole row is the tap target, and it commits [membership]
 * toward the larger end ([ListMembership.NONE] and [ListMembership.PARTIAL] both move to
 * [ListMembership.ALL], [ListMembership.ALL] clears to [ListMembership.NONE]).
 *
 * [membership] picks the chip's look, the leading bookmark glyph and the caption's ink.
 */
@Immutable
data class ChooseListsRowUiModel(
    val listId: Int,
    val name: String,
    val caption: String,
    val chip: ChipUiModel,
    val membership: ListMembership,
    /** A mutation is in flight: the row is tap-disabled and shows the house wavy indicator, not a circular spinner. */
    val isPending: Boolean,
)

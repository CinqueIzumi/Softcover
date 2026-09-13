package nl.rhaydus.softcover.core.component.lists

import androidx.compose.runtime.Immutable

/**
 * One ruled-index row in the choose-lists sheet.
 *
 * [caption] and [actionLabel] arrive already written — "12 BOOKS · 3 OF 5 HERE", "Add the other 2" —
 * because composing them needs the selection size and how much of it the list already holds, which is
 * the feature's arithmetic, not the row's (R4).
 *
 * [membership] stays on the model even though the labels are resolved, because it chooses *which*
 * trailing control renders: three genuinely different chromes (a removable filled chip, a filled
 * add pill, a quiet outline pill), not three labels on one.
 */
@Immutable
data class ChooseListsRowUiModel(
    val listId: Int,
    val name: String,
    val caption: String,
    val actionLabel: String,
    val membership: ListMembership,
    /** A mutation is in flight: the row shows a spinner and ignores taps. */
    val isPending: Boolean,
)

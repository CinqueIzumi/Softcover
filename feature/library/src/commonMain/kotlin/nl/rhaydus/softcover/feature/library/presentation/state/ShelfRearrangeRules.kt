package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab

/**
 * The status whose manual order a drag on [tab] rewrites, or `null` when [tab] is not being
 * rearranged. Only a Status tab other than Did Not Finish on [LibrarySortMode.MANUAL] qualifies, and
 * never during selection.
 */
internal fun bookReorderStatusFor(
    tab: LibraryTab,
    sortMode: LibrarySortMode,
    selectionMode: Boolean,
    isRearranging: Boolean,
): UserBookStatus? = if (
    sortMode == LibrarySortMode.MANUAL &&
    tab is LibraryTab.Status &&
    tab.status != UserBookStatus.DID_NOT_FINISH &&
    selectionMode.not() &&
    isRearranging
) {
    tab.status
} else {
    null
}

/** Whether a custom list is being rearranged: [LibrarySortMode.ORDER] on a ranked list. */
internal fun isEditionListRearranging(
    sortMode: LibrarySortMode,
    isRanked: Boolean,
    isRearranging: Boolean,
): Boolean = sortMode == LibrarySortMode.ORDER && isRanked && isRearranging

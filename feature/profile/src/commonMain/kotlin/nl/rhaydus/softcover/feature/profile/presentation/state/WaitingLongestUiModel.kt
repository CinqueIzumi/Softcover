package nl.rhaydus.softcover.feature.profile.presentation.state

import nl.rhaydus.softcover.core.component.bookcard.BookCardSet

/**
 * The Waiting the longest block: the book waiting longest on Want to Read, then the owned, unstarted one
 * waiting longest on the shelf, in [cards] order. With none, [cards] is empty and [caption] says what would
 * show here.
 */
internal data class WaitingLongestUiModel(
    val title: String,
    val caption: String,
    val cards: BookCardSet<WaitingEntry>,
    val isLoading: Boolean,
)

package nl.rhaydus.softcover.feature.profile.presentation.state

import nl.rhaydus.softcover.core.component.bookcard.BookCardSet

/**
 * The Your records section: the longest book and the quickest read in scope, in [cards] order.
 *
 * @property hasRecords false when neither record exists, so the cards are dropped and [caption] says why.
 */
internal data class ReadingRecordsUiModel(
    val title: String,
    val caption: String,
    val cards: BookCardSet<ReadingRecordEntry>,
    val hasRecords: Boolean,
    val isLoading: Boolean,
)

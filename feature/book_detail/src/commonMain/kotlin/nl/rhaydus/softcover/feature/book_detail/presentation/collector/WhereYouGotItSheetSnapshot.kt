package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import nl.rhaydus.softcover.feature.book_detail.presentation.state.WhereYouGotItDraft

/** The state fields the where-you-got-it sheet's UI model is derived from. */
internal data class WhereYouGotItSheetSnapshot(
    val bookTitle: String?,
    val draft: WhereYouGotItDraft?,
)

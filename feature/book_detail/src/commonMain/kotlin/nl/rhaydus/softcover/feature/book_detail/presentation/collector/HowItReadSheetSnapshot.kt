package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import nl.rhaydus.softcover.feature.book_detail.presentation.state.HowItReadDraft

/** The state fields the how-it-read sheet's UI model is derived from. */
internal data class HowItReadSheetSnapshot(
    val bookTitle: String?,
    val draft: HowItReadDraft?,
)

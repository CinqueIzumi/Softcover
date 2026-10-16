package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.book_detail.presentation.state.QuoteDraft

/** The state fields the quote sheet's UI model is derived from. */
internal data class QuoteSheetSnapshot(
    val book: Book?,
    val draft: QuoteDraft?,
)

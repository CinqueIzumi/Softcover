package nl.rhaydus.softcover.core.component.quote

sealed interface QuoteCardEvent {
    /** The reader chose Delete from the card's menu; the host confirms or removes the quote. */
    data class DeleteRequested(val key: Long) : QuoteCardEvent
}

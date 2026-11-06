package nl.rhaydus.softcover.core.component.quote

sealed interface QuoteCardEvent {
    /** The reader chose Delete from a saved quote's menu; the host confirms or removes the quote. */
    data class DeleteRequested(val key: Long) : QuoteCardEvent

    /** Only a [QuoteCardUiModel.Book] card emits it. */
    data object Clicked : QuoteCardEvent
}

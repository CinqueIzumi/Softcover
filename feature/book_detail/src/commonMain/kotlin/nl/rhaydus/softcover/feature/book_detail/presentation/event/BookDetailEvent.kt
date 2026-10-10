package nl.rhaydus.softcover.feature.book_detail.presentation.event

import nl.rhaydus.toad.UiEvent

internal sealed interface BookDetailEvent : UiEvent {
    class BookMarkedAsReadEvent : BookDetailEvent

    data class OpenExternalLinkEvent(val url: String) : BookDetailEvent

    class RefreshDetailBookEvent : BookDetailEvent

    class ShelfChangedEvent : BookDetailEvent

    class ProgressUpdatedEvent : BookDetailEvent

    class MutationFailedEvent : BookDetailEvent
}

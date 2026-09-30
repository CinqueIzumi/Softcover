package nl.rhaydus.softcover.feature.scan.presentation.event

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.toad.UiEvent

internal sealed interface ScanEvent : UiEvent {
    class AddBookFailedEvent : ScanEvent

    data class BookResolvedEvent(
        val book: Book,
        val editionId: Int?,
    ) : ScanEvent

    class InvalidIsbnEvent : ScanEvent

    class ResolutionFailedEvent : ScanEvent
}

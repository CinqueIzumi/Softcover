package nl.rhaydus.softcover.feature.scan.presentation.event

import nl.rhaydus.softcover.core.component.haptics.Feedback
import nl.rhaydus.softcover.core.component.haptics.FeedbackEvent
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.toad.UiEvent

internal sealed interface ScanEvent : UiEvent {
    class AddBookFailedEvent :
        ScanEvent,
        FeedbackEvent {
        override val feedback = Feedback.Failure
    }

    data class BookResolvedEvent(
        val book: Book,
        val editionId: Int?,
    ) : ScanEvent,
        FeedbackEvent {
        override val feedback = Feedback.Routine
    }

    class InvalidIsbnEvent :
        ScanEvent,
        FeedbackEvent {
        override val feedback = Feedback.Failure
    }

    class ResolutionFailedEvent :
        ScanEvent,
        FeedbackEvent {
        override val feedback = Feedback.Failure
    }
}

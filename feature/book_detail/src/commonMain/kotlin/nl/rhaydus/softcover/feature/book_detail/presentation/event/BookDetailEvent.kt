package nl.rhaydus.softcover.feature.book_detail.presentation.event

import nl.rhaydus.softcover.core.component.haptics.Feedback
import nl.rhaydus.softcover.core.component.haptics.FeedbackEvent
import nl.rhaydus.toad.UiEvent

internal sealed interface BookDetailEvent : UiEvent {
    class BookMarkedAsReadEvent : BookDetailEvent, FeedbackEvent {
        override val feedback = Feedback.Milestone
    }

    data class OpenExternalLinkEvent(val url: String) : BookDetailEvent

    class RefreshDetailBookEvent : BookDetailEvent

    class ShelfChangedEvent : BookDetailEvent, FeedbackEvent {
        override val feedback = Feedback.Routine
    }

    class ProgressUpdatedEvent : BookDetailEvent, FeedbackEvent {
        override val feedback = Feedback.Routine
    }

    class MutationSucceededEvent(override val feedback: Feedback?) : BookDetailEvent, FeedbackEvent

    /** Only for failures the hero and edition shakes do not already cover. */
    class MutationFailedEvent : BookDetailEvent, FeedbackEvent {
        override val feedback = Feedback.Failure
    }

    class CreateListRequestedEvent : BookDetailEvent
}

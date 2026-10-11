package nl.rhaydus.softcover.feature.library.presentation.event

import nl.rhaydus.softcover.core.component.haptics.Feedback
import nl.rhaydus.softcover.core.component.haptics.FeedbackEvent
import nl.rhaydus.toad.UiEvent

internal sealed interface LibraryEvent : UiEvent {
    class BulkMoveAppliedEvent(override val feedback: Feedback) : LibraryEvent, FeedbackEvent

    class BulkMoveFailedEvent : LibraryEvent, FeedbackEvent {
        override val feedback = Feedback.Failure
    }

    class ListMembershipChangedEvent(override val feedback: Feedback?) : LibraryEvent, FeedbackEvent

    class ListMembershipChangeFailedEvent : LibraryEvent, FeedbackEvent {
        override val feedback = Feedback.Failure
    }

    class NewListBooksAddFailedEvent : LibraryEvent, FeedbackEvent {
        override val feedback = Feedback.Failure
    }

    class ListReorderFailedEvent : LibraryEvent, FeedbackEvent {
        override val feedback = Feedback.Failure
    }

    class ListRankedChangeFailedEvent : LibraryEvent, FeedbackEvent {
        override val feedback = Feedback.Failure
    }

    class ManageShelvesRequestedEvent : LibraryEvent

    class CreateListRequestedEvent : LibraryEvent
}

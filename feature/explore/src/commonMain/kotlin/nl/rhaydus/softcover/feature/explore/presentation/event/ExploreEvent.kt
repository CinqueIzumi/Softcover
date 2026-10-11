package nl.rhaydus.softcover.feature.explore.presentation.event

import nl.rhaydus.softcover.core.component.haptics.Feedback
import nl.rhaydus.softcover.core.component.haptics.FeedbackEvent
import nl.rhaydus.toad.UiEvent

internal sealed interface ExploreEvent : UiEvent {
    class BookAddedEvent :
        ExploreEvent,
        FeedbackEvent {
        override val feedback = Feedback.Routine
    }

    class AddBookFailedEvent :
        ExploreEvent,
        FeedbackEvent {
        override val feedback = Feedback.Failure
    }
}

package nl.rhaydus.softcover.feature.lists.presentation.event

import nl.rhaydus.softcover.core.component.haptics.Feedback
import nl.rhaydus.softcover.core.component.haptics.FeedbackEvent
import nl.rhaydus.toad.UiEvent

internal sealed interface CreateListEvent : UiEvent {
    data class ListCreatedEvent(
        val listId: Int,
        val name: String,
    ) : CreateListEvent,
        FeedbackEvent {
        override val feedback = Feedback.Milestone
    }

    data class ListCreationFailedEvent(
        val message: String?,
    ) : CreateListEvent,
        FeedbackEvent {
        override val feedback = Feedback.Failure
    }
}

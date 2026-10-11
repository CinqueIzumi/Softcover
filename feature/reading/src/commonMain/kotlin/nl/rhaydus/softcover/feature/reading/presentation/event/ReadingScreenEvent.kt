package nl.rhaydus.softcover.feature.reading.presentation.event

import nl.rhaydus.softcover.core.component.haptics.Feedback
import nl.rhaydus.softcover.core.component.haptics.FeedbackEvent
import nl.rhaydus.toad.UiEvent

internal sealed interface ReadingScreenEvent : UiEvent {
    class VerdictSavedEvent : ReadingScreenEvent, FeedbackEvent {
        override val feedback = Feedback.Routine
    }

    class ProgressUpdatedEvent : ReadingScreenEvent, FeedbackEvent {
        override val feedback = Feedback.Routine
    }
}

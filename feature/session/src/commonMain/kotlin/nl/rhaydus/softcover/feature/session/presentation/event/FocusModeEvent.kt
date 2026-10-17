package nl.rhaydus.softcover.feature.session.presentation.event

import nl.rhaydus.softcover.core.component.haptics.Feedback
import nl.rhaydus.softcover.core.component.haptics.FeedbackEvent
import nl.rhaydus.toad.UiEvent

internal sealed interface FocusModeEvent : UiEvent {
    class PageCommittedEvent :
        FocusModeEvent,
        FeedbackEvent {
        override val feedback = Feedback.Routine
    }
}

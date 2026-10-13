package nl.rhaydus.softcover.feature.profile.presentation.event

import nl.rhaydus.softcover.core.component.haptics.Feedback
import nl.rhaydus.softcover.core.component.haptics.FeedbackEvent
import nl.rhaydus.toad.UiEvent

internal sealed interface ProfileEvent : UiEvent {
    class LogOutUserEvent : ProfileEvent

    class LogOutFailedEvent :
        ProfileEvent,
        FeedbackEvent {
        override val feedback = Feedback.Failure
    }
}

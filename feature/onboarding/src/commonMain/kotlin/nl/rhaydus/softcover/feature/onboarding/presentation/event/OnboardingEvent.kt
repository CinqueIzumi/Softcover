package nl.rhaydus.softcover.feature.onboarding.presentation.event

import nl.rhaydus.softcover.core.component.haptics.Feedback
import nl.rhaydus.softcover.core.component.haptics.FeedbackEvent
import nl.rhaydus.toad.UiEvent

internal sealed interface OnboardingEvent : UiEvent {
    class SignedInEvent :
        OnboardingEvent,
        FeedbackEvent {
        override val feedback = Feedback.Routine
    }

    class SignInFailedEvent :
        OnboardingEvent,
        FeedbackEvent {
        override val feedback = Feedback.Failure
    }

    class SignInCompletedEvent : OnboardingEvent
}

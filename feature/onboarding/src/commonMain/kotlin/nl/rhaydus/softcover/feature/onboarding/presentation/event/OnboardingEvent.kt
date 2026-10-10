package nl.rhaydus.softcover.feature.onboarding.presentation.event

import nl.rhaydus.toad.UiEvent

internal sealed interface OnboardingEvent : UiEvent {
    class SignedInEvent : OnboardingEvent

    class ApiKeyRejectedEvent : OnboardingEvent

    class SignInCompletedEvent : OnboardingEvent
}

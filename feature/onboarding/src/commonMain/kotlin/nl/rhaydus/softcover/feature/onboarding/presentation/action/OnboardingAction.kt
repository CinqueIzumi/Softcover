package nl.rhaydus.softcover.feature.onboarding.presentation.action

import nl.rhaydus.softcover.feature.onboarding.presentation.event.OnboardingEvent
import nl.rhaydus.softcover.feature.onboarding.presentation.screenmodel.OnboardingDependencies
import nl.rhaydus.softcover.feature.onboarding.presentation.state.LocalOnboardingVariables
import nl.rhaydus.softcover.feature.onboarding.presentation.state.OnboardingUiState
import nl.rhaydus.toad.UiAction

internal sealed interface OnboardingAction : UiAction<
        OnboardingDependencies,
        OnboardingUiState,
        OnboardingEvent,
        LocalOnboardingVariables,
        >

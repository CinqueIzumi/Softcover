package nl.rhaydus.softcover.feature.onboarding.presentation.action

import nl.rhaydus.softcover.core.presentation.toad.UiAction
import nl.rhaydus.softcover.feature.onboarding.presentation.state.OnboardingUiState
import nl.rhaydus.softcover.feature.onboarding.presentation.event.OnboardingEvent
import nl.rhaydus.softcover.feature.onboarding.presentation.state.LocalOnboardingVariables
import nl.rhaydus.softcover.feature.onboarding.presentation.viewmodel.OnboardingDependencies

sealed interface OnboardingAction : UiAction<
        OnboardingDependencies,
        OnboardingUiState,
        OnboardingEvent,
        LocalOnboardingVariables,
        >
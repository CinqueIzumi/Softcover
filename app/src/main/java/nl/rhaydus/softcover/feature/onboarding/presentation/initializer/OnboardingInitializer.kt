package nl.rhaydus.softcover.feature.onboarding.presentation.initializer

import nl.rhaydus.softcover.core.presentation.toad.Initializer
import nl.rhaydus.softcover.feature.onboarding.presentation.state.OnboardingUiState
import nl.rhaydus.softcover.feature.onboarding.presentation.event.OnboardingEvent
import nl.rhaydus.softcover.feature.onboarding.presentation.state.LocalOnboardingVariables
import nl.rhaydus.softcover.feature.onboarding.presentation.viewmodel.OnboardingDependencies

sealed interface OnboardingInitializer : Initializer<
        OnboardingUiState,
        OnboardingEvent,
        OnboardingDependencies,
        LocalOnboardingVariables,
        >
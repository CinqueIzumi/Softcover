package nl.rhaydus.softcover.feature.updated_onboarding.presentation.initializer

import nl.rhaydus.softcover.core.presentation.toad.Initializer
import nl.rhaydus.softcover.feature.updated_onboarding.presentation.state.OnboardingUiState
import nl.rhaydus.softcover.feature.updated_onboarding.presentation.event.OnboardingEvent
import nl.rhaydus.softcover.feature.updated_onboarding.presentation.state.LocalOnboardingVariables
import nl.rhaydus.softcover.feature.updated_onboarding.presentation.screenmodel.OnboardingDependencies

sealed interface OnboardingInitializer : Initializer<
        OnboardingUiState,
        OnboardingEvent,
        OnboardingDependencies,
        LocalOnboardingVariables,
        >
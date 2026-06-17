package nl.rhaydus.softcover.feature.onboarding.presentation.flows

import nl.rhaydus.softcover.feature.onboarding.presentation.event.OnboardingEvent
import nl.rhaydus.softcover.feature.onboarding.presentation.screenmodel.OnboardingDependencies
import nl.rhaydus.softcover.feature.onboarding.presentation.state.LocalOnboardingVariables
import nl.rhaydus.softcover.feature.onboarding.presentation.state.OnboardingUiState
import nl.rhaydus.toad.Collector

internal sealed interface OnboardingInitializer : Collector<
        OnboardingUiState,
        OnboardingEvent,
        OnboardingDependencies,
        LocalOnboardingVariables,
        >

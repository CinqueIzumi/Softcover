package nl.rhaydus.softcover.feature.onboarding.presentation.flows

import nl.rhaydus.softcover.core.designsystem.presentation.toad.Collector
import nl.rhaydus.softcover.feature.onboarding.presentation.event.OnboardingEvent
import nl.rhaydus.softcover.feature.onboarding.presentation.screenmodel.OnboardingDependencies
import nl.rhaydus.softcover.feature.onboarding.presentation.state.LocalOnboardingVariables
import nl.rhaydus.softcover.feature.onboarding.presentation.state.OnboardingUiState

internal sealed interface OnboardingInitializer : Collector<
        OnboardingUiState,
        OnboardingEvent,
        OnboardingDependencies,
        LocalOnboardingVariables,
        >

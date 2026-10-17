package nl.rhaydus.softcover.feature.onboarding.presentation.action

import nl.rhaydus.softcover.feature.onboarding.presentation.event.OnboardingEvent
import nl.rhaydus.softcover.feature.onboarding.presentation.screenmodel.OnboardingDependencies
import nl.rhaydus.softcover.feature.onboarding.presentation.state.LocalOnboardingVariables
import nl.rhaydus.softcover.feature.onboarding.presentation.state.OnboardingUiState
import nl.rhaydus.toad.ActionScope

internal data object OnApiKeyShakeEndedAction : OnboardingAction {
    override suspend fun execute(
        dependencies: OnboardingDependencies,
        scope: ActionScope<OnboardingUiState, OnboardingEvent, LocalOnboardingVariables>,
    ) {
        scope.setState {
            it.copy(apiKeyField = it.apiKeyField.copy(error = it.apiKeyField.error?.copy(shaking = false)))
        }
    }
}

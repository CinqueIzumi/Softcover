package nl.rhaydus.softcover.feature.onboarding.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.onboarding.presentation.event.OnboardingEvent
import nl.rhaydus.softcover.feature.onboarding.presentation.screenmodel.OnboardingDependencies
import nl.rhaydus.softcover.feature.onboarding.presentation.state.LocalOnboardingVariables
import nl.rhaydus.softcover.feature.onboarding.presentation.state.OnboardingUiState

class OnApiKeyValueChangeAction(val newValue: String) : OnboardingAction {
    override suspend fun execute(
        dependencies: OnboardingDependencies,
        scope: ActionScope<OnboardingUiState, OnboardingEvent, LocalOnboardingVariables>,
    ) {
        scope.setState {
            it.copy(apiKeyValue = newValue)
        }
    }
}
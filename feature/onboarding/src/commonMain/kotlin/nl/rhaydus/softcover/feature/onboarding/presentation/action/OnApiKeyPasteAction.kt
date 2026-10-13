package nl.rhaydus.softcover.feature.onboarding.presentation.action

import nl.rhaydus.softcover.feature.onboarding.presentation.event.OnboardingEvent
import nl.rhaydus.softcover.feature.onboarding.presentation.screenmodel.OnboardingDependencies
import nl.rhaydus.softcover.feature.onboarding.presentation.state.LocalOnboardingVariables
import nl.rhaydus.softcover.feature.onboarding.presentation.state.OnboardingUiState
import nl.rhaydus.toad.ActionScope

internal data class OnApiKeyPasteAction(val pastedText: String) : OnboardingAction {
    override suspend fun execute(
        dependencies: OnboardingDependencies,
        scope: ActionScope<OnboardingUiState, OnboardingEvent, LocalOnboardingVariables>,
    ) {
        if (pastedText.isBlank()) return

        OnApiKeyValueChangeAction(newValue = pastedText).execute(
            dependencies = dependencies,
            scope = scope,
        )
    }
}

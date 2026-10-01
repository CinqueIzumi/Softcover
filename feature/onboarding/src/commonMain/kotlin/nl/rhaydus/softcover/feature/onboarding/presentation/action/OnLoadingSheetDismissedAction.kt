package nl.rhaydus.softcover.feature.onboarding.presentation.action

import nl.rhaydus.softcover.feature.onboarding.presentation.event.OnboardingEvent
import nl.rhaydus.softcover.feature.onboarding.presentation.screenmodel.OnboardingDependencies
import nl.rhaydus.softcover.feature.onboarding.presentation.state.LocalOnboardingVariables
import nl.rhaydus.softcover.feature.onboarding.presentation.state.OnboardingUiState
import nl.rhaydus.toad.ActionScope

internal data object OnLoadingSheetDismissedAction : OnboardingAction {
    override suspend fun execute(
        dependencies: OnboardingDependencies,
        scope: ActionScope<OnboardingUiState, OnboardingEvent, LocalOnboardingVariables>,
    ) {
        scope.setState {
            it.copy(
                loadingSheet = it.loadingSheet.copy(
                    isLoading = false,
                    scaffold = it.loadingSheet.scaffold.copy(closeRequested = false),
                ),
            )
        }
    }
}

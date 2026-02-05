package nl.rhaydus.softcover.feature.onboarding.presentation.action

import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.presentation.util.SnackBarManager
import nl.rhaydus.softcover.feature.onboarding.presentation.event.OnboardingEvent
import nl.rhaydus.softcover.feature.onboarding.presentation.screenmodel.OnboardingDependencies
import nl.rhaydus.softcover.feature.onboarding.presentation.state.LocalOnboardingVariables
import nl.rhaydus.softcover.feature.onboarding.presentation.state.OnboardingUiState
import timber.log.Timber

class OnApiKeySaveClickAction() : OnboardingAction {
    override suspend fun execute(
        dependencies: OnboardingDependencies,
        scope: ActionScope<OnboardingUiState, OnboardingEvent, LocalOnboardingVariables>,
    ) {
        scope.setState {
            it.copy(
                saveApiKeyButtonEnabled = false,
                isLoading = true,
            )
        }

        val updatedKey = scope.currentState.apiKeyValue
            .removePrefix("Bearer")
            .trim()

        dependencies.launch {
            dependencies.resetUserDataUseCase().onFailure {
                Timber.e("-=- Something went wrong while resetting user's data! $it")
            }

            dependencies.updateApiKeyUseCase(key = updatedKey).onSuccess {
                dependencies.initializeUserDataUseCase().onFailure {
                    Timber.e("-=- $it")
                    SnackBarManager.showSnackbar(title = "Something went wrong while trying to initialize the user's profile.")
                }
            }.onFailure { Timber.e("-=- $it") }

            scope.setState {
                it.copy(
                    saveApiKeyButtonEnabled = true,
                    isLoading = false,
                )
            }
        }
    }
}
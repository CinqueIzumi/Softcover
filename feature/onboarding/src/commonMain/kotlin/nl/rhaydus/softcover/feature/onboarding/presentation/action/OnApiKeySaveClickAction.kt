package nl.rhaydus.softcover.feature.onboarding.presentation.action

import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.domain.logging.AppLog
import nl.rhaydus.softcover.feature.onboarding.presentation.event.OnboardingEvent
import nl.rhaydus.softcover.feature.onboarding.presentation.screenmodel.OnboardingDependencies
import nl.rhaydus.softcover.feature.onboarding.presentation.state.LocalOnboardingVariables
import nl.rhaydus.softcover.feature.onboarding.presentation.state.OnboardingUiState
import nl.rhaydus.toad.ActionScope

internal class OnApiKeySaveClickAction : OnboardingAction {
    private lateinit var scope: ActionScope<OnboardingUiState, OnboardingEvent, LocalOnboardingVariables>

    override suspend fun execute(
        dependencies: OnboardingDependencies,
        scope: ActionScope<OnboardingUiState, OnboardingEvent, LocalOnboardingVariables>,
    ) {
        this.scope = scope

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
            val resetDateSuccessFully = dependencies.resetUserDataUseCase()
                .onFailure { AppLog.e("Resetting failed $it") }
                .isSuccess

            if (resetDateSuccessFully.not()) {
                stopLoading()
                return@launch
            }

            scope.setState { it.copy(progress = 0.1f) }

            val updatedApiKeySuccessfully = dependencies.updateApiKeyUseCase(key = updatedKey)
                .onFailure { AppLog.e("$it") }
                .isSuccess

            if (updatedApiKeySuccessfully.not()) {
                stopLoading()
                return@launch
            }

            scope.setState { it.copy(progress = 0.2f) }

            val initializedUserDataSuccessfully = dependencies.initializeUserIdAndBooksUseCase()
                .onFailure {
                    AppLog.e("Initialize user id use case $it")

                    // TODO: Snack bar message is no longer shown when modal loader is active...
                    SnackBarManager.showSnackbar(title = "Something went wrong while trying to initialize the user's profile.")
                }
                .isSuccess

            when {
                initializedUserDataSuccessfully -> scope.setState { it.copy(progress = 1f) }
                else -> stopLoading()
            }
        }
    }

    private fun stopLoading() {
        scope.setState {
            it.copy(
                saveApiKeyButtonEnabled = true,
                isLoading = false,
                progress = 0f,
            )
        }
    }
}

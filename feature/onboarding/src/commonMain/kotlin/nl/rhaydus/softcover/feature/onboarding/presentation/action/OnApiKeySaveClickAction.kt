package nl.rhaydus.softcover.feature.onboarding.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.error.toUserMessage
import nl.rhaydus.softcover.core.domain.exception.InvalidTokenException
import nl.rhaydus.softcover.feature.onboarding.presentation.event.OnboardingEvent
import nl.rhaydus.softcover.feature.onboarding.presentation.screenmodel.OnboardingDependencies
import nl.rhaydus.softcover.feature.onboarding.presentation.state.LocalOnboardingVariables
import nl.rhaydus.softcover.feature.onboarding.presentation.state.OnboardingUiState
import nl.rhaydus.toad.ActionScope

internal data object OnApiKeySaveClickAction : OnboardingAction {
    override suspend fun execute(
        dependencies: OnboardingDependencies,
        scope: ActionScope<OnboardingUiState, OnboardingEvent, LocalOnboardingVariables>,
    ) {
        fun stopLoading(error: Throwable) {
            scope.setState {
                it.copy(
                    saveApiKeyButtonEnabled = true,
                    isLoading = false,
                    progress = 0f,
                    submissionError = mapError(error),
                )
            }
        }

        scope.setState {
            it.copy(
                saveApiKeyButtonEnabled = false,
                isLoading = true,
                submissionError = null,
            )
        }

        val updatedKey = scope.currentState.apiKeyValue
            .removePrefix("Bearer")
            .trim()

        dependencies.launch {
            dependencies.resetUserDataUseCase().onFailure { error ->
                stopLoading(error)
                return@launch
            }

            scope.setState { it.copy(progress = 0.1f) }

            dependencies.updateApiKeyUseCase(key = updatedKey).onFailure { error ->
                stopLoading(error)
                return@launch
            }

            scope.setState { it.copy(progress = 0.2f) }

            dependencies.initializeUserIdAndBooksUseCase()
                .onFailure { error ->
                    stopLoading(error)
                    return@launch
                }
                .onSuccess { scope.setState { it.copy(progress = 1f) } }
        }
    }

    private fun mapError(error: Throwable): String = when (error) {
        is InvalidTokenException -> "That API key wasn't accepted. Double-check it and try again."
        else -> error.toUserMessage() ?: "Something went wrong setting up your account. Please try again."
    }
}

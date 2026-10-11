package nl.rhaydus.softcover.feature.onboarding.presentation.action

import nl.rhaydus.softcover.core.component.field.TextFieldError
import nl.rhaydus.softcover.core.component.sheet.open
import nl.rhaydus.softcover.core.component.sheet.requestClose
import nl.rhaydus.softcover.core.component.sheet.withModel
import nl.rhaydus.softcover.core.domain.exception.InvalidTokenException
import nl.rhaydus.softcover.core.presentation.error.toUserMessage
import nl.rhaydus.softcover.feature.onboarding.presentation.event.OnboardingEvent
import nl.rhaydus.softcover.feature.onboarding.presentation.screenmodel.OnboardingDependencies
import nl.rhaydus.softcover.feature.onboarding.presentation.state.LocalOnboardingVariables
import nl.rhaydus.softcover.feature.onboarding.presentation.state.OnboardingUiState
import nl.rhaydus.softcover.feature.onboarding.presentation.state.onboardingLoadingSheet
import nl.rhaydus.toad.ActionScope

internal data object OnApiKeySaveClickAction : OnboardingAction {
    override suspend fun execute(
        dependencies: OnboardingDependencies,
        scope: ActionScope<OnboardingUiState, OnboardingEvent, LocalOnboardingVariables>,
    ) {
        if (scope.currentState.saveApiKeyButtonEnabled.not()) return

        fun stopLoading(error: Throwable) {
            val rejectedKey = error is InvalidTokenException

            scope.setState {
                it.copy(
                    saveApiKeyButtonEnabled = it.apiKeyField.text.isNotBlank(),
                    loadingSheet = it.loadingSheet
                        .withModel { sheet -> sheet.copy(progress = 0f) }
                        .requestClose(),
                    signInFailedBefore = true,
                    apiKeyField = if (rejectedKey) {
                        it.apiKeyField.copy(error = TextFieldError(message = REJECTED_KEY_MESSAGE))
                    } else {
                        it.apiKeyField
                    },
                    submissionError = if (rejectedKey) null else mapError(error),
                )
            }

            if (rejectedKey.not()) scope.sendEvent(event = OnboardingEvent.SignInFailedEvent())
        }

        scope.setState {
            it.copy(
                saveApiKeyButtonEnabled = false,
                loadingSheet = it.loadingSheet.open(
                    model = onboardingLoadingSheet(progress = if (it.signInFailedBefore) 0f else null),
                ),
                submissionError = null,
            )
        }

        val updatedKey = scope.currentState.apiKeyField.text
            .removePrefix("Bearer")
            .trim()

        dependencies.launch {
            dependencies.resetUserDataUseCase().onFailure { error ->
                stopLoading(error)
                return@launch
            }

            scope.setState {
                it.copy(loadingSheet = it.loadingSheet.withModel { sheet -> sheet.copy(progress = 0.1f) })
            }

            dependencies.updateApiKeyUseCase(key = updatedKey).onFailure { error ->
                stopLoading(error)
                return@launch
            }

            scope.setState {
                it.copy(loadingSheet = it.loadingSheet.withModel { sheet -> sheet.copy(progress = 0.2f) })
            }

            dependencies.initializeUserIdAndBooksUseCase()
                .onFailure { error ->
                    stopLoading(error)
                    return@launch
                }
                .onSuccess {
                    scope.setState {
                        it.copy(loadingSheet = it.loadingSheet.withModel { sheet -> sheet.copy(progress = 1f) })
                    }

                    scope.sendEvent(event = OnboardingEvent.SignedInEvent())
                }
        }
    }

    private const val REJECTED_KEY_MESSAGE = "That API key wasn't accepted. Double-check it and try again."

    private fun mapError(error: Throwable): String =
        error.toUserMessage() ?: "Something went wrong setting up your account. Please try again."
}

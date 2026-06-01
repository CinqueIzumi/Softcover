package nl.rhaydus.softcover.feature.onboarding.presentation.state

import nl.rhaydus.softcover.core.presentation.toad.UiState

data class OnboardingUiState(
    val apiKeyValue: String = "",
    val saveApiKeyButtonEnabled: Boolean = true,
    val isLoading: Boolean = false,
    val progress: Float? = null,
) : UiState
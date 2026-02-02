package nl.rhaydus.softcover.feature.onboarding.presentation.state

import nl.rhaydus.softcover.core.presentation.toad.UiState

data class OnboardingUiState(
    val apiKeyValue: String = "",
    ) : UiState
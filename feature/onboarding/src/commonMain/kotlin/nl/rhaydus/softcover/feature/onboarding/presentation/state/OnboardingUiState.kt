package nl.rhaydus.softcover.feature.onboarding.presentation.state

import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.LoadingSheetUiModel
import nl.rhaydus.toad.UiState

internal data class OnboardingUiState(
    val apiKeyValue: String = "",
    val saveApiKeyButtonEnabled: Boolean = true,
    val submissionError: String? = null,
    val loadingSheet: LoadingSheetUiModel = LoadingSheetUiModel(
        header = SectionHeaderUiModel.Section(
            eyebrow = "Setting up",
            headline = "Pulling your library together.",
            description = "Depending on its size, this might take a moment.",
        ),
        progress = null,
        isLoading = false,
    ),
) : UiState

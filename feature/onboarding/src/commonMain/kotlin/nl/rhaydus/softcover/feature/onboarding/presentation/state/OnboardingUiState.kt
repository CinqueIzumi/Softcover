package nl.rhaydus.softcover.feature.onboarding.presentation.state

import nl.rhaydus.softcover.core.component.divider.DividerCenter
import nl.rhaydus.softcover.core.component.divider.DividerUiModel
import nl.rhaydus.softcover.core.component.divider.HorizontalDividerSurface
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.LoadingSheetUiModel
import nl.rhaydus.toad.UiState

internal data class OnboardingUiState(
    val apiKeyValue: String = "",
    val saveApiKeyButtonEnabled: Boolean = true,
    val submissionError: String? = null,
    val orTypeItDivider: DividerUiModel = DividerUiModel.Horizontal(
        surface = HorizontalDividerSurface.OnboardingOrTypeIt,
        center = DividerCenter.Label(text = "Or type it"),
    ),
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

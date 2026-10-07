package nl.rhaydus.softcover.feature.onboarding.presentation.state

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.field.TextFieldUiModel
import nl.rhaydus.softcover.core.component.field.TextFieldVariant
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.LoadingSheetUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.feature.onboarding.presentation.model.IntroScreen
import nl.rhaydus.toad.UiState

internal data class OnboardingUiState(
    val apiKeyField: TextFieldUiModel = TextFieldUiModel(
        key = "api-key",
        text = "",
        placeholder = "Enter your key",
        variant = TextFieldVariant.Pill(),
        submittable = true,
    ),
    val saveApiKeyButtonEnabled: Boolean = false,
    val submissionError: String? = null,
    val loadingSheet: LoadingSheetUiModel = LoadingSheetUiModel(
        scaffold = SheetScaffoldUiModel(
            header = SheetHeaderUiModel(
                section = SectionHeaderUiModel.Section(
                    eyebrow = "Setting up",
                    headline = "Pulling your library together.",
                    description = "Depending on its size, this might take a moment.",
                ),
            ),
            dismissible = false,
        ),
        progress = null,
        isLoading = false,
    ),

    /** The persistent top bar's header for each pager page. */
    val pageHeaders: ImmutableMap<IntroScreen, SectionHeaderUiModel.Section> = persistentMapOf(
        IntroScreen.FIRST to SectionHeaderUiModel.Section(eyebrow = "Welcome"),
        IntroScreen.SECOND to SectionHeaderUiModel.Section(eyebrow = "Connect"),
        IntroScreen.THIRD to SectionHeaderUiModel.Section(eyebrow = "API key"),
    ),
) : UiState

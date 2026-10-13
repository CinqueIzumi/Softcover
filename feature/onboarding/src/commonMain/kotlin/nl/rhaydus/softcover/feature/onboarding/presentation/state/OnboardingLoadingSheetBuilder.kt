package nl.rhaydus.softcover.feature.onboarding.presentation.state

import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.LoadingSheetUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

internal fun onboardingLoadingSheet(progress: Float?): LoadingSheetUiModel =
    LoadingSheetUiModel(
        scaffold = SheetScaffoldUiModel(
            header = SheetHeaderUiModel(
                section = SectionHeaderUiModel.Section(
                    eyebrow = "Setting up",
                    headline = "Getting your shelves ready…",
                    description = "Depending on its size, this might take a moment.",
                ),
            ),
            dismissible = false,
        ),
        progress = progress,
    )

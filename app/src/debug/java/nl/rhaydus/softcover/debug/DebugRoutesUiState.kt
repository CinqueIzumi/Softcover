package nl.rhaydus.softcover.debug

import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.toad.UiState

data class DebugRoutesUiState(
    val pageHeader: SectionHeaderUiModel.Section = SectionHeaderUiModel.Section(
        eyebrow = "Debug",
        headline = "Behind the scenes.",
        description = "Debug-only previews compiled out of release builds.",
    ),
) : UiState

package nl.rhaydus.softcover.debug

import nl.rhaydus.softcover.core.component.divider.DividerUiModel
import nl.rhaydus.softcover.core.component.divider.DividerVariant
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.toad.UiState

data class DebugRoutesUiState(
    val pageHeader: SectionHeaderUiModel.Section = SectionHeaderUiModel.Section(
        eyebrow = "Debug",
        headline = "Behind the scenes.",
        description = "Debug-only previews compiled out of release builds.",
    ),
    val rowDivider: DividerUiModel = DividerUiModel(DividerVariant.DebugRoutesRow),
) : UiState

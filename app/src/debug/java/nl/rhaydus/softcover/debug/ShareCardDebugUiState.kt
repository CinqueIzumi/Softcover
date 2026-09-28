package nl.rhaydus.softcover.debug

import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.toad.UiState

data class ShareCardDebugUiState(
    val pageHeader: SectionHeaderUiModel.Section = SectionHeaderUiModel.Section(
        eyebrow = "Debug",
        headline = "Share card variants.",
        description = "Tap a card's “Save to gallery” button to write the rendered PNG to " +
            "Pictures/Softcover. On Android 9 and below the system will ask for storage permission first.",
    ),
) : UiState

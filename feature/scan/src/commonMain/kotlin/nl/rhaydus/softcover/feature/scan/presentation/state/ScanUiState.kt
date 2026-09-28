package nl.rhaydus.softcover.feature.scan.presentation.state

import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.toad.UiState

internal data class ScanUiState(
    val isResolving: Boolean = false,
    val unknownIsbn: String? = null,
    val isAddingBook: Boolean = false,
    val unknownIsbnHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Not in Hardcover",
        headline = "We don't have this one yet.",
        description = "Double-check the ISBN below — if it's right, add it to Hardcover so you can track it.",
    ),
    val manualEntryHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "By ISBN",
        headline = "Enter the ISBN.",
        description = "Type the ISBN-10 or ISBN-13 printed near the barcode.",
    ),
) : UiState

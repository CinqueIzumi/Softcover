package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel

internal data class LibraryArrangeChips(
    val layoutChips: List<ChipUiModel>,
    val sortChips: List<ChipUiModel>,
    val sortHeader: SectionHeaderUiModel,
) {
    companion object {
        val Empty = LibraryArrangeChips(
            layoutChips = emptyList(),
            sortChips = emptyList(),
            sortHeader = SectionHeaderUiModel.Label(eyebrow = "Sort"),
        )
    }
}

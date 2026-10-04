package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode

internal data class LibraryArrangeChips(
    val layoutChips: ChipSet<LibraryLayoutChip>,
    val sortChips: ChipSet<LibrarySortMode>,
    val sortHeader: SectionHeaderUiModel,
) {
    companion object {
        val Empty = LibraryArrangeChips(
            layoutChips = ChipSet(),
            sortChips = ChipSet(),
            sortHeader = SectionHeaderUiModel.Label(eyebrow = "Sort"),
        )
    }
}

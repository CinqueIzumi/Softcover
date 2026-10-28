package nl.rhaydus.softcover.feature.library.presentation.state

import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.field.TextFieldUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel

/**
 * One tag source's section of the Filter sheet. [chosenLabel] is null and [chosenChips] empty while nothing
 * of the source is chosen; [categoryChips] and [matchHeader] are empty and null while the search is blank.
 */
internal data class TagSourceSummary(
    val source: TagSource,
    val header: SectionHeaderUiModel,
    val chosenLabel: String?,
    val gloss: String?,
    val chosenChips: ImmutableList<ChipUiModel>,
    val searchField: TextFieldUiModel,
    val categoryChips: ImmutableList<ChipUiModel>,
    val matchHeader: SectionHeaderUiModel?,
    val tagChips: ImmutableList<ChipUiModel>,
    val showMoreChip: ChipUiModel,
    val showLessChip: ChipUiModel,
    val emptyLine: String?,
)

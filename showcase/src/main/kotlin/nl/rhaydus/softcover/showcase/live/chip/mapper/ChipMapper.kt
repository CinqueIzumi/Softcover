package nl.rhaydus.softcover.showcase.live.chip.mapper

import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentMap
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.chip.ChipSize
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipTrailing
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.chip.toChipSet
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.showcase.live.chip.model.ChipSectionUiModel
import nl.rhaydus.softcover.showcase.live.chip.model.ChipSelectionMode
import nl.rhaydus.softcover.showcase.live.chip.state.ChipLiveUiState

internal fun chipLiveState(): ChipLiveUiState = ChipLiveUiState(
    topBar = TopBarUiModel(title = "Chips"),
    sections = listOf(
        chipSection(
            key = "tonal",
            header = SectionHeaderUiModel.Section(
                eyebrow = "Tonal",
                headline = "As many as you like",
                description = "Each chip toggles on its own.",
            ),
            mode = ChipSelectionMode.Multiple,
            tone = ChipTone.Tonal,
            size = ChipSize.Regular,
            labels = GENRES,
        ),
        chipSection(
            key = "tonal-compact",
            header = SectionHeaderUiModel.Section(
                eyebrow = "Tonal, compact",
                description = "The same chips, set tighter for a crowded row.",
            ),
            mode = ChipSelectionMode.Multiple,
            tone = ChipTone.Tonal,
            size = ChipSize.Compact,
            labels = GENRES,
        ),
        chipSection(
            key = "choice",
            header = SectionHeaderUiModel.Section(
                eyebrow = "Choice",
                headline = "One at a time",
                description = "Picking a chip lets go of the last.",
            ),
            mode = ChipSelectionMode.Single,
            tone = ChipTone.Choice,
            size = ChipSize.Regular,
            labels = SORTS,
        ),
        chipSection(
            key = "choice-compact",
            header = SectionHeaderUiModel.Section(
                eyebrow = "Choice, compact",
                description = "A single choice, in the smaller size.",
            ),
            mode = ChipSelectionMode.Single,
            tone = ChipTone.Choice,
            size = ChipSize.Compact,
            labels = SORTS,
        ),
        chipSection(
            key = "removable",
            header = SectionHeaderUiModel.Section(
                eyebrow = "Removable",
                headline = "Tap the cross",
                description = "A dismissed chip leaves the set for good.",
            ),
            mode = ChipSelectionMode.Removable,
            tone = ChipTone.Container,
            size = ChipSize.Regular,
            labels = TAGS,
        ),
    ).toImmutableList(),
)

internal fun ChipSectionUiModel.afterClick(chipKey: String): ChipSectionUiModel {
    val chips = when (mode) {
        ChipSelectionMode.Multiple -> set.chips.map {
            if (it.key == chipKey) it.copy(selected = it.selected.not()) else it
        }
        ChipSelectionMode.Single -> set.chips.map { it.copy(selected = it.key == chipKey) }
        ChipSelectionMode.Removable -> set.chips
    }

    return copy(set = set.copy(chips = chips.toImmutableList()))
}

internal fun ChipSectionUiModel.afterDismiss(chipKey: String): ChipSectionUiModel = copy(
    set = ChipSet(
        chips = set.chips.filterNot { it.key == chipKey }.toImmutableList(),
        payloadByKey = set.payloadByKey.toPersistentMap().remove(chipKey),
    ),
)

private val GENRES = listOf("Fantasy", "Cozy mystery", "Memoir", "Poetry", "Nineteenth-century classics", "Graphic novels")
private val SORTS = listOf("Recently added", "Title", "Author", "Rating")
private val TAGS = listOf("Second helpings", "Slow burn", "Found family", "Unreliable narrator")

private fun chipSection(
    key: String,
    header: SectionHeaderUiModel,
    mode: ChipSelectionMode,
    tone: ChipTone,
    size: ChipSize,
    labels: List<String>,
): ChipSectionUiModel = ChipSectionUiModel(
    key = key,
    header = header,
    mode = mode,
    set = labels
        .mapIndexed { index, label ->
            ChipUiModel(
                key = label,
                label = label,
                tone = tone,
                size = size,
                selected = mode != ChipSelectionMode.Removable && index == 0,
                trailing = if (mode == ChipSelectionMode.Removable) {
                    ChipTrailing.Dismiss(label = "Remove $label")
                } else {
                    null
                },
            ) to label
        }
        .toChipSet(),
)

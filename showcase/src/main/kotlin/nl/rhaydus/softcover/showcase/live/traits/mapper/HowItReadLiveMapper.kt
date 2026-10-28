package nl.rhaydus.softcover.showcase.live.traits.mapper

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.component.traits.HowItReadBlockUiModel
import nl.rhaydus.softcover.core.component.traits.HowItReadChipGroupUiModel
import nl.rhaydus.softcover.core.component.traits.HowItReadSheetUiModel

internal fun howItReadLiveTopBar(): TopBarUiModel = TopBarUiModel(title = "How it read")

internal fun howItReadHeader(): SectionHeaderUiModel = SectionHeaderUiModel.Label(eyebrow = "How it read")

internal fun howItReadEmptyBlock(): HowItReadBlockUiModel = HowItReadBlockUiModel.previews.getValue("Empty")

internal fun HowItReadSheetUiModel.hasAnswers(): Boolean =
    balance.value != null || listOf(pace, characters, writingStyle).any { group ->
        group.rows.any { row -> row.chips.any { chip -> chip.selected || chip.tone == ChipTone.TonalDashed } }
    }

internal fun howItReadBlock(expanded: Boolean): HowItReadBlockUiModel = HowItReadBlockUiModel.previews.getValue(
    if (expanded) "Filled, expanded" else "Filled, collapsed",
)

internal fun howItReadSheet(): HowItReadSheetUiModel = HowItReadSheetUiModel.previews.getValue("Empty draft")

internal fun HowItReadSheetUiModel.withBalance(value: Float?): HowItReadSheetUiModel = copy(
    balance = balance.copy(value = value),
)

/**
 * Applies a tap on [key]: pace and the writing style choice are single choice and clear when re-tapped, the
 * characters cycle off, yes, complicated, and the writing flags toggle.
 */
internal fun HowItReadSheetUiModel.withChipClicked(key: String): HowItReadSheetUiModel = copy(
    pace = pace.mapRows { it.choosing(key = key) },
    characters = characters.mapRows { it.cycling(key = key) },
    writingStyle = writingStyle.copy(
        rows = writingStyle.rows
            .mapIndexed { index, row ->
                if (index == 0) row.choosing(key = key) else row.toggling(key = key)
            }
            .toImmutableList(),
    ),
)

private fun HowItReadChipGroupUiModel.mapRows(transform: (ChipSet<String>) -> ChipSet<String>) = copy(
    rows = rows.map(transform).toImmutableList(),
)

private fun ChipSet<String>.choosing(key: String): ChipSet<String> =
    if (get(key) == null) {
        this
    } else {
        withChips { chip -> chip.copy(selected = chip.key == key && chip.selected.not()) }
    }

private fun ChipSet<String>.toggling(key: String): ChipSet<String> = withChips { chip ->
    if (chip.key == key) chip.copy(selected = chip.selected.not()) else chip
}

private fun ChipSet<String>.cycling(key: String): ChipSet<String> = withChips { chip ->
    when {
        chip.key != key -> chip

        chip.tone == ChipTone.TonalDashed -> chip.copy(tone = ChipTone.Tonal)

        chip.selected -> chip.copy(
            selected = false,
            tone = ChipTone.TonalDashed,
        )

        else -> chip.copy(selected = true)
    }
}

private fun ChipSet<String>.withChips(transform: (ChipUiModel) -> ChipUiModel): ChipSet<String> = copy(
    chips = chips.map(transform).toImmutableList(),
)

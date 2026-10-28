package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.chip.toChipSet
import nl.rhaydus.softcover.core.component.traits.HowItReadBalanceUiModel
import nl.rhaydus.softcover.core.component.traits.HowItReadChipGroupUiModel
import nl.rhaydus.softcover.core.component.traits.HowItReadSheetUiModel
import nl.rhaydus.softcover.core.domain.model.CharacterTrait
import nl.rhaydus.softcover.core.domain.model.Pace
import nl.rhaydus.softcover.core.domain.model.WritingFlag
import nl.rhaydus.softcover.core.domain.model.WritingStyle
import nl.rhaydus.softcover.feature.book_detail.presentation.state.HowItReadAnswer
import nl.rhaydus.softcover.feature.book_detail.presentation.state.HowItReadDraft

internal fun HowItReadDraft.toHowItReadSheet(bookTitle: String): HowItReadSheetUiModel {
    val yes = characters.filterValues { it == HowItReadAnswer.YES }.keys
    val selected = yes + listOfNotNull(paceKey, writingStyleKey) + flagKeys
    val complicated = characters.filterValues { it == HowItReadAnswer.COMPLICATED }.keys

    fun chipRow(options: List<Pair<String, String>>): ChipSet<String> = options
        .map { (key, label) ->
            ChipUiModel(
                key = key,
                label = label,
                selected = key in selected,
                tone = if (key in complicated) ChipTone.TonalDashed else ChipTone.Tonal,
            ) to key
        }
        .toChipSet()

    return HowItReadSheetUiModel(
        pace = HowItReadChipGroupUiModel(
            label = "Pace",
            rows = persistentListOf(chipRow(Pace.entries.map { it.toKey() to it.toLabel() })),
        ),
        balance = HowItReadBalanceUiModel(
            label = "Plot or character",
            value = balance,
            startLabel = "Plot",
            centreLabel = "A mix",
            endLabel = "Character",
            clearLabel = "Clear",
            unsetLabel = "Not answered",
        ),
        characters = HowItReadChipGroupUiModel(
            label = "The characters were",
            rows = persistentListOf(chipRow(CharacterTrait.entries.map { it.toKey() to it.toChipLabel() })),
            caption = "Tap once for yes, again for complicated, a third time to clear.",
        ),
        writingStyle = HowItReadChipGroupUiModel(
            label = "Writing style",
            rows = persistentListOf(
                chipRow(WritingStyle.entries.map { it.toKey() to it.toLabel() }),
                chipRow(WritingFlag.entries.map { it.toKey() to it.toLabel() }),
            ),
        ),
        scaffold = HowItReadSheetUiModel.scaffoldFor(bookTitle = bookTitle),
    )
}

private fun CharacterTrait.toChipLabel(): String = when (this) {
    CharacterTrait.BIPOC, CharacterTrait.LGBTQ -> "${toLabel()} characters"
    else -> toLabel()
}

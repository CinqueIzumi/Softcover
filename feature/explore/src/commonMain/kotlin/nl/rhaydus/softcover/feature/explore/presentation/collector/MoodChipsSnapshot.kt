package nl.rhaydus.softcover.feature.explore.presentation.collector

import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.chip.toChipSet
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.softcover.feature.explore.presentation.util.toTitleCaseWords

private const val MOOD_CHIP_KEY_PREFIX = "mood:"

internal data class MoodChipsSnapshot(
    val moodTags: List<MoodTag>,
) {
    fun compute(): ChipSet<MoodTag> = moodTags.map { mood ->
        val chip = ChipUiModel(
            key = MOOD_CHIP_KEY_PREFIX + mood.id,
            label = mood.label.toTitleCaseWords(),
        )

        chip to mood
    }.toChipSet()
}

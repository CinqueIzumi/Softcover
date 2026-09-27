package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.softcover.feature.explore.presentation.util.toTitleCaseWords

private const val MOOD_CHIP_KEY_PREFIX = "mood:"

internal data class MoodChipsSnapshot(
    val moodTags: List<MoodTag>,
) {
    fun compute(): Pair<ImmutableList<ChipUiModel>, Map<String, MoodTag>> {
        val entries = moodTags.map { mood ->
            val chip = ChipUiModel(
                key = MOOD_CHIP_KEY_PREFIX + mood.id,
                label = mood.label.toTitleCaseWords(),
            )

            chip to mood
        }

        val chips = entries.map { it.first }.toImmutableList()
        val moodByKey = entries.associate { (chip, mood) -> chip.key to mood }

        return chips to moodByKey
    }
}

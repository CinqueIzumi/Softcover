package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.feeling.HowItFeltBlockUiModel
import nl.rhaydus.softcover.core.component.feeling.HowItFeltMomentUiModel
import nl.rhaydus.softcover.core.component.feeling.HowItFeltMoodUiModel
import nl.rhaydus.softcover.core.component.feeling.MoodRibbonMarkUiModel
import nl.rhaydus.softcover.core.component.feeling.MoodRibbonUiModel
import nl.rhaydus.softcover.core.domain.model.BookMood
import nl.rhaydus.softcover.core.domain.model.Mood
import nl.rhaydus.softcover.core.domain.model.MoodMoment
import nl.rhaydus.softcover.core.domain.model.MoodWord
import nl.rhaydus.softcover.core.uibinding.mood.moodMomentAt
import nl.rhaydus.softcover.core.uibinding.mood.toChipKey
import nl.rhaydus.softcover.core.uibinding.mood.toLabel

private const val PERCENT_MAX = 100

internal fun toHowItFeltBlock(
    moods: List<BookMood>,
    moments: List<MoodMoment>,
): HowItFeltBlockUiModel = HowItFeltBlockUiModel(
    title = "How it felt",
    editLabel = "Edit",
    invitation = "Nothing here yet. Tap Edit to say how it felt, in your own words.",
    moods = moods
        .sortedBy { it.word.presetOrder() }
        .map {
            HowItFeltMoodUiModel(
                key = it.word.toChipKey(),
                label = it.word.toLabel(),
                strength = it.strength,
            )
        }
        .toImmutableList(),
    momentsTitle = "Along the way",
    moments = moments
        .sortedByPosition()
        .map {
            HowItFeltMomentUiModel(
                id = it.id.toString(),
                at = moodMomentAt(
                    page = it.page,
                    seconds = it.seconds,
                    percent = it.percent,
                ),
                label = it.word.toLabel(),
                strength = it.strength,
            )
        }
        .toImmutableList(),
    caption = "Added when you update your progress.",
)

/** Null when no moment has a percent to sit at. */
internal fun List<MoodMoment>.toMoodRibbon(): MoodRibbonUiModel? {
    val placed = sortedByPosition().filter { it.percent != null }

    if (placed.isEmpty()) return null

    return MoodRibbonUiModel(
        marks = placed
            .map {
                MoodRibbonMarkUiModel(
                    word = it.word.toLabel().lowercase(),
                    position = (it.percent ?: 0) / PERCENT_MAX.toFloat(),
                    strength = it.strength,
                )
            }
            .toImmutableList(),
        contentDescription = placed.joinToString(
            prefix = "Moods along the way: ",
            separator = ", ",
        ) { "${it.word.toLabel().lowercase()} ${it.percent.toRibbonPlace()}" },
    )
}

private fun List<MoodMoment>.sortedByPosition(): List<MoodMoment> = sortedWith(
    compareBy<MoodMoment, Int?>(nullsLast()) { it.percent }.thenBy { it.recordedAt },
)

private fun Int?.toRibbonPlace(): String = when (this) {
    0 -> "at the start"
    PERCENT_MAX -> "at the end"
    else -> "at $this%"
}

private fun MoodWord.presetOrder(): Int = when (this) {
    is MoodWord.Preset -> Mood.HowItFelt.indexOf(mood).takeIf { it >= 0 } ?: Mood.HowItFelt.size
    is MoodWord.Own -> Int.MAX_VALUE
}

package nl.rhaydus.softcover.core.component.feeling

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/** What [MoodRibbon] renders. [contentDescription] names every mood and its position in one sentence. */
@Immutable
data class MoodRibbonUiModel(
    val marks: ImmutableList<MoodRibbonMarkUiModel>,
    val contentDescription: String,
) {
    companion object : UiModelPreviews<MoodRibbonUiModel> {
        override val previews: ImmutableMap<String, MoodRibbonUiModel> = persistentMapOf(
            "Three moods" to MoodRibbonUiModel(
                marks = persistentListOf(
                    MoodRibbonMarkUiModel(
                        word = "curious",
                        position = 0.18f,
                        strength = 50,
                    ),
                    MoodRibbonMarkUiModel(
                        word = "tense",
                        position = 0.38f,
                        strength = 70,
                    ),
                    MoodRibbonMarkUiModel(
                        word = "wrecked",
                        position = 0.62f,
                        strength = 90,
                    ),
                ),
                contentDescription = "Moods along the way: curious at 18%, tense at 38%, wrecked at 62%",
            ),
            "Strength unset, at the edges" to MoodRibbonUiModel(
                marks = persistentListOf(
                    MoodRibbonMarkUiModel(
                        word = "hopeful",
                        position = 0f,
                        strength = null,
                    ),
                    MoodRibbonMarkUiModel(
                        word = "satisfied",
                        position = 1f,
                        strength = 80,
                    ),
                ),
                contentDescription = "Moods along the way: hopeful at the start, satisfied at the end",
            ),
        )
    }
}

/** A mood at [position], a fraction of the bar from 0 to 1. A null [strength] draws a lighter dot. */
@Immutable
data class MoodRibbonMarkUiModel(
    val word: String,
    val position: Float,
    val strength: Int?,
)

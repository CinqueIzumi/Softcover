package nl.rhaydus.softcover.core.component.chart

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Labelled scores, each a value over a hairline track filled against the full scale rather than the lead
 * row. [ScoreBarUiModel.valueContentDescription], when set, is spoken in place of the value text, for a value
 * text such as a dash that reads badly aloud.
 *
 * While [isLoading] the rows keep their layout and mask their text and tracks.
 */
@Immutable
data class ScoreBarsUiModel(
    val rows: ImmutableList<ScoreBarUiModel>,
    val isLoading: Boolean = false,
) {
    companion object : UiModelPreviews<ScoreBarsUiModel> {
        override val previews: ImmutableMap<String, ScoreBarsUiModel> = persistentMapOf(
            "Scores" to ScoreBarsUiModel(
                rows = persistentListOf(
                    ScoreBarUiModel(
                        label = "Pace",
                        valueText = "3.4",
                        fill = 0.68f,
                    ),
                    ScoreBarUiModel(
                        label = "Emotion",
                        valueText = "4.1",
                        fill = 0.82f,
                    ),
                    ScoreBarUiModel(
                        label = "Humour",
                        valueText = "2.0",
                        fill = 0.4f,
                    ),
                ),
            ),
            "With a missing value" to ScoreBarsUiModel(
                rows = persistentListOf(
                    ScoreBarUiModel(
                        label = "Pace",
                        valueText = "3.4",
                        fill = 0.68f,
                    ),
                    ScoreBarUiModel(
                        label = "Strength",
                        valueText = "–",
                        fill = 0f,
                        valueContentDescription = "no strength given",
                    ),
                ),
            ),
            "Loading" to loading(rowCount = 3),
        )

        fun loading(rowCount: Int): ScoreBarsUiModel = ScoreBarsUiModel(
            rows = List(rowCount) {
                ScoreBarUiModel(
                    label = "",
                    valueText = "",
                    fill = 0f,
                )
            }.toImmutableList(),
            isLoading = true,
        )
    }
}

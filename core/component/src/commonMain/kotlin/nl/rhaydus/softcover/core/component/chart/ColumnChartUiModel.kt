package nl.rhaydus.softcover.core.component.chart

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Vertical columns sharing one baseline, each sized against the tallest. The value row above the plot
 * renders only when some column carries a value.
 *
 * While [isLoading] the columns keep their layout and mask their bars and text.
 */
@Immutable
data class ColumnChartUiModel(
    val columns: ImmutableList<ColumnUiModel>,
    val isLoading: Boolean = false,
) {
    companion object : UiModelPreviews<ColumnChartUiModel> {
        private const val RATING_STEP_COUNT = 9
        private const val ZERO_RATING_STEP = 1
        private const val LOADING_COLUMN_COUNT = 8
        private val RATING_FRACTIONS = listOf(0.05f, 0f, 0.1f, 0.15f, 0.2f, 0.45f, 0.4f, 1f, 0.7f)

        override val previews: ImmutableList<ColumnChartUiModel> = persistentListOf(
            ColumnChartUiModel(
                columns = persistentListOf(
                    ColumnUiModel(
                        fraction = 0.3f,
                        tone = ChartTone.Second,
                        label = "'18",
                        value = "24",
                    ),
                    ColumnUiModel(
                        fraction = 0.5f,
                        tone = ChartTone.Second,
                        label = "'19",
                        value = "40",
                    ),
                    ColumnUiModel(
                        fraction = 0.45f,
                        tone = ChartTone.Second,
                        label = "'20",
                        value = "36",
                    ),
                    ColumnUiModel(
                        fraction = 0f,
                        tone = ChartTone.Fifth,
                        label = "'21",
                        value = null,
                    ),
                    ColumnUiModel(
                        fraction = 0.7f,
                        tone = ChartTone.Second,
                        label = "'22",
                        value = "56",
                    ),
                    ColumnUiModel(
                        fraction = 1f,
                        tone = ChartTone.Lead,
                        label = "'23",
                        value = "80",
                    ),
                    ColumnUiModel(
                        fraction = 0.6f,
                        tone = ChartTone.Second,
                        label = "'24",
                        value = "48",
                    ),
                    ColumnUiModel(
                        fraction = 0.2f,
                        tone = ChartTone.Second,
                        label = "'25",
                        value = "16",
                    ),
                ),
            ),
            ColumnChartUiModel(
                columns = List(RATING_STEP_COUNT) { index ->
                    val isWholeStep = index % 2 == 0

                    ColumnUiModel(
                        fraction = if (index == ZERO_RATING_STEP) 0f else RATING_FRACTIONS[index],
                        tone = when {
                            index == ZERO_RATING_STEP -> ChartTone.Fifth
                            isWholeStep -> ChartTone.Lead
                            else -> ChartTone.Fourth
                        },
                        label = if (isWholeStep) "${index / 2 + 1}★" else null,
                        value = null,
                    )
                }.toImmutableList(),
            ),
            ColumnChartUiModel(
                columns = List(LOADING_COLUMN_COUNT) {
                    ColumnUiModel(
                        fraction = 0.5f,
                        tone = ChartTone.Second,
                        label = "",
                        value = "",
                    )
                }.toImmutableList(),
                isLoading = true,
            ),
        )
    }
}

package nl.rhaydus.softcover.core.component.chart

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Vertical columns sharing one baseline, each sized against the tallest. [showsValues] keeps the value
 * row above the plot even when every column's value is null, so the chart's height does not depend on
 * its data.
 *
 * While [isLoading] the columns keep their layout and mask their bars and text.
 */
@Immutable
data class ColumnChartUiModel(
    val columns: ImmutableList<ColumnUiModel>,
    val showsValues: Boolean,
    val isLoading: Boolean = false,
) {
    companion object : UiModelPreviews<ColumnChartUiModel> {
        private const val RATING_STEP_COUNT = 9
        private const val ZERO_RATING_STEP = 1
        private const val LOADING_COLUMN_COUNT = 8
        private const val YEAR_MONTH_COUNT = 12
        private const val LABEL_STRIDE = 3
        private val MONTH_LABELS = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        private val MONTH_FRACTIONS = listOf(0.4f, 0.2f, 1f, 0.5f, 0.1f, 0f, 0.3f, 0.6f, 0.45f, 0.7f, 0.35f, 0.25f)
        private val RATING_FRACTIONS = listOf(0.05f, 0f, 0.1f, 0.15f, 0.2f, 0.45f, 0.4f, 1f, 0.7f)

        override val previews: ImmutableMap<String, ColumnChartUiModel> = persistentMapOf(
            "8 columns" to ColumnChartUiModel(
                columns = persistentListOf(
                    ColumnUiModel(
                        fraction = 0.3f,
                        tone = ChartTone.Second,
                        label = "'18",
                        value = "24",
                        contentDescription = "2018, 24 books",
                    ),
                    ColumnUiModel(
                        fraction = 0.5f,
                        tone = ChartTone.Second,
                        label = "'19",
                        value = "40",
                        contentDescription = "2019, 40 books",
                    ),
                    ColumnUiModel(
                        fraction = 0.45f,
                        tone = ChartTone.Second,
                        label = "'20",
                        value = "36",
                        contentDescription = "2020, 36 books",
                    ),
                    ColumnUiModel(
                        fraction = 0f,
                        tone = ChartTone.Fifth,
                        label = "'21",
                        value = null,
                        contentDescription = "2021, 0 books",
                    ),
                    ColumnUiModel(
                        fraction = 0.7f,
                        tone = ChartTone.Second,
                        label = "'22",
                        value = "56",
                        contentDescription = "2022, 56 books",
                    ),
                    ColumnUiModel(
                        fraction = 1f,
                        tone = ChartTone.Lead,
                        label = "'23",
                        value = "80",
                        contentDescription = "2023, 80 books",
                    ),
                    ColumnUiModel(
                        fraction = 0.6f,
                        tone = ChartTone.Second,
                        label = "'24",
                        value = "48",
                        contentDescription = "2024, 48 books",
                    ),
                    ColumnUiModel(
                        fraction = 0.2f,
                        tone = ChartTone.Second,
                        label = "'25",
                        value = "16",
                        contentDescription = "2025, 16 books",
                    ),
                ),
                showsValues = true,
            ),
            "9 columns" to ColumnChartUiModel(
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
                        contentDescription = when {
                            index == 0 -> "1 star"
                            isWholeStep -> "${index / 2 + 1} stars"
                            else -> "${index / 2 + 1}½ stars"
                        },
                    )
                }.toImmutableList(),
                showsValues = false,
            ),
            "12 months" to monthColumns(count = YEAR_MONTH_COUNT),
            "13 months" to monthColumns(count = YEAR_MONTH_COUNT + 1),
            "Loading" to ColumnChartUiModel(
                columns = List(LOADING_COLUMN_COUNT) {
                    ColumnUiModel(
                        fraction = 0.5f,
                        tone = ChartTone.Second,
                        label = "",
                        value = "",
                        contentDescription = "",
                    )
                }.toImmutableList(),
                showsValues = true,
                isLoading = true,
            ),
        )

        private fun monthColumns(count: Int): ColumnChartUiModel = ColumnChartUiModel(
            columns = List(count) { index ->
                val fraction = MONTH_FRACTIONS[index % YEAR_MONTH_COUNT]

                ColumnUiModel(
                    fraction = fraction,
                    tone = when {
                        fraction == 1f -> ChartTone.Lead
                        fraction == 0f -> ChartTone.Fifth
                        else -> ChartTone.Third
                    },
                    label = MONTH_LABELS[index % YEAR_MONTH_COUNT].takeIf { index % LABEL_STRIDE == 0 },
                    value = "1,480".takeIf { fraction == 1f },
                    contentDescription = MONTH_LABELS[index % YEAR_MONTH_COUNT],
                )
            }.toImmutableList(),
            showsValues = true,
        )
    }
}

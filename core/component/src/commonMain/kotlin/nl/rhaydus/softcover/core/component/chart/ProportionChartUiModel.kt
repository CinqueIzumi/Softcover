package nl.rhaydus.softcover.core.component.chart

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * One stacked bar of parts of a whole, with its legend beneath as rows or inline keys.
 *
 * While [isLoading] the bar masks as one block and the legend keeps its rows with masked text and swatches.
 */
@Immutable
data class ProportionChartUiModel(
    val segments: ImmutableList<ChartSliceUiModel>,
    val isLoading: Boolean = false,
    val variant: ProportionChartVariant = ProportionChartVariant.Rows,
) {
    companion object : UiModelPreviews<ProportionChartUiModel> {
        override val previews: ImmutableMap<String, ProportionChartUiModel> = persistentMapOf(
            "Known segments" to ProportionChartUiModel(
                segments = persistentListOf(
                    ChartSliceUiModel(
                        label = "Women",
                        share = "58%",
                        fraction = 0.58f,
                        tone = ChartTone.Lead,
                    ),
                    ChartSliceUiModel(
                        label = "Men",
                        share = "34%",
                        fraction = 0.34f,
                        tone = ChartTone.Second,
                    ),
                    ChartSliceUiModel(
                        label = "Other",
                        share = "8%",
                        fraction = 0.08f,
                        tone = ChartTone.Third,
                    ),
                ),
            ),
            "With Unknown" to ProportionChartUiModel(
                segments = persistentListOf(
                    ChartSliceUiModel(
                        label = "Yes",
                        share = "30%",
                        fraction = 0.3f,
                        tone = ChartTone.Lead,
                    ),
                    ChartSliceUiModel(
                        label = "No",
                        share = "50%",
                        fraction = 0.5f,
                        tone = ChartTone.Fourth,
                    ),
                    ChartSliceUiModel(
                        label = "Unknown",
                        share = "20%",
                        fraction = 0.2f,
                        tone = ChartTone.Unknown,
                    ),
                ),
            ),
            "Zero segment" to ProportionChartUiModel(
                segments = persistentListOf(
                    ChartSliceUiModel(
                        label = "Yes",
                        share = "0%",
                        fraction = 0f,
                        tone = ChartTone.Lead,
                    ),
                    ChartSliceUiModel(
                        label = "No",
                        share = "100%",
                        fraction = 1f,
                        tone = ChartTone.Fourth,
                    ),
                ),
            ),
            "Loading" to ProportionChartUiModel(
                segments = persistentListOf(
                    loadingSegment(ChartTone.Lead),
                    loadingSegment(ChartTone.Second),
                    loadingSegment(ChartTone.Third),
                ),
                isLoading = true,
            ),
            "Inline keys" to ProportionChartUiModel(
                segments = persistentListOf(
                    ChartSliceUiModel(
                        label = "Print",
                        share = "52%",
                        fraction = 0.52f,
                        tone = ChartTone.Lead,
                    ),
                    ChartSliceUiModel(
                        label = "Ebook",
                        share = "31%",
                        fraction = 0.31f,
                        tone = ChartTone.Second,
                    ),
                    ChartSliceUiModel(
                        label = "Audiobook",
                        share = "17%",
                        fraction = 0.17f,
                        tone = ChartTone.Third,
                    ),
                ),
                variant = ProportionChartVariant.Keys,
            ),
            "Inline keys loading" to ProportionChartUiModel(
                segments = persistentListOf(
                    loadingSegment(ChartTone.Lead),
                    loadingSegment(ChartTone.Second),
                    loadingSegment(ChartTone.Third),
                ),
                isLoading = true,
                variant = ProportionChartVariant.Keys,
            ),
        )

        private fun loadingSegment(tone: ChartTone) = ChartSliceUiModel(
            label = "",
            share = "",
            fraction = 0f,
            tone = tone,
        )
    }
}

package nl.rhaydus.softcover.core.component.chart

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Horizontal bars, each measured against its own 100% track rather than against its neighbours.
 *
 * While [isLoading] the rows keep their layout and mask their text and tracks.
 */
@Immutable
data class RankedBarsUiModel(
    val rows: ImmutableList<ChartSliceUiModel>,
    val isLoading: Boolean = false,
) {
    companion object : UiModelPreviews<RankedBarsUiModel> {
        override val previews: ImmutableMap<String, RankedBarsUiModel> = persistentMapOf(
            "5 rows" to RankedBarsUiModel(
                rows = persistentListOf(
                    ChartSliceUiModel(
                        label = "Fantasy",
                        share = "62%",
                        fraction = 0.62f,
                        tone = ChartTone.Lead,
                    ),
                    ChartSliceUiModel(
                        label = "Science fiction",
                        share = "41%",
                        fraction = 0.41f,
                        tone = ChartTone.Second,
                    ),
                    ChartSliceUiModel(
                        label = "Literary fiction",
                        share = "27%",
                        fraction = 0.27f,
                        tone = ChartTone.Third,
                    ),
                    ChartSliceUiModel(
                        label = "Historical",
                        share = "12%",
                        fraction = 0.12f,
                        tone = ChartTone.Fourth,
                    ),
                    ChartSliceUiModel(
                        label = "Poetry",
                        share = "1%",
                        fraction = 0.01f,
                        tone = ChartTone.Fifth,
                    ),
                ),
            ),
            "Loading" to RankedBarsUiModel(
                rows = persistentListOf(
                    loadingRow(ChartTone.Lead),
                    loadingRow(ChartTone.Second),
                    loadingRow(ChartTone.Third),
                ),
                isLoading = true,
            ),
        )

        private fun loadingRow(tone: ChartTone) = ChartSliceUiModel(
            label = "",
            share = "",
            fraction = 0f,
            tone = tone,
        )
    }
}

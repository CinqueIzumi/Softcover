package nl.rhaydus.softcover.core.component.chart

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Horizontal bars, each measured against its own 100% track rather than against its neighbours.
 *
 * While [isLoading] the rows keep their layout and mask their text and tracks.
 */
@Immutable
data class RankedBarsUiModel(
    val rows: ImmutableList<RankedBarUiModel>,
    val isLoading: Boolean = false,
) {
    companion object : UiModelPreviews<RankedBarsUiModel> {
        override val previews: ImmutableList<RankedBarsUiModel> = persistentListOf(
            RankedBarsUiModel(
                rows = persistentListOf(
                    RankedBarUiModel(
                        label = "Fantasy",
                        share = "62%",
                        fraction = 0.62f,
                        tone = ChartTone.Lead,
                    ),
                    RankedBarUiModel(
                        label = "Science fiction",
                        share = "41%",
                        fraction = 0.41f,
                        tone = ChartTone.Second,
                    ),
                    RankedBarUiModel(
                        label = "Literary fiction",
                        share = "27%",
                        fraction = 0.27f,
                        tone = ChartTone.Third,
                    ),
                    RankedBarUiModel(
                        label = "Historical",
                        share = "12%",
                        fraction = 0.12f,
                        tone = ChartTone.Fourth,
                    ),
                    RankedBarUiModel(
                        label = "Poetry",
                        share = "1%",
                        fraction = 0.01f,
                        tone = ChartTone.Fifth,
                    ),
                ),
            ),
            RankedBarsUiModel(
                rows = persistentListOf(
                    loadingRow(ChartTone.Lead),
                    loadingRow(ChartTone.Second),
                    loadingRow(ChartTone.Third),
                ),
                isLoading = true,
            ),
        )

        private fun loadingRow(tone: ChartTone) = RankedBarUiModel(
            label = "",
            share = "",
            fraction = 0f,
            tone = tone,
        )
    }
}

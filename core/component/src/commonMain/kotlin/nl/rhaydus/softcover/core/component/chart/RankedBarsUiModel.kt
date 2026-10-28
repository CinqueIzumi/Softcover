package nl.rhaydus.softcover.core.component.chart

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Numbered horizontal bars, each filled relative to the lead row rather than to a common 100%.
 *
 * While [isLoading] the rows keep their layout and mask their numbers, text and tracks.
 *
 * [rows] is the complete ranking; rows from [visibleCount] on are folded away.
 */
@Immutable
data class RankedBarsUiModel(
    val rows: ImmutableList<RankedBarUiModel>,
    val visibleCount: Int,
    val isLoading: Boolean = false,
) {
    companion object : UiModelPreviews<RankedBarsUiModel> {
        override val previews: ImmutableMap<String, RankedBarsUiModel> = persistentMapOf(
            "Filled" to RankedBarsUiModel(
                rows = persistentListOf(
                    RankedBarUiModel(
                        label = "Fantasy",
                        share = "62%",
                        fill = 1f,
                    ),
                    RankedBarUiModel(
                        label = "Science fiction",
                        share = "41%",
                        fill = 0.66f,
                    ),
                    RankedBarUiModel(
                        label = "Literary fiction",
                        share = "27%",
                        fill = 0.44f,
                    ),
                ),
                visibleCount = 3,
            ),
            "Loading" to RankedBarsUiModel(
                rows = persistentListOf(
                    loadingRow(),
                    loadingRow(),
                    loadingRow(),
                ),
                visibleCount = 3,
                isLoading = true,
            ),
        )

        private fun loadingRow() = RankedBarUiModel(
            label = "",
            share = "",
            fill = 0f,
        )
    }
}

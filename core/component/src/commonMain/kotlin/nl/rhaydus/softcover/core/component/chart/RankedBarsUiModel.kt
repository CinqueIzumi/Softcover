package nl.rhaydus.softcover.core.component.chart

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Numbered horizontal bars, each filled relative to the lead row rather than to a common 100%.
 *
 * While [isLoading] the rows keep their layout and mask their numbers, text and tracks.
 *
 * While [areRowsTappable] and not [isLoading] each row reports a click.
 *
 * [rows] is the complete ranking; rows from [visibleCount] on are folded away.
 */
@Immutable
data class RankedBarsUiModel(
    val rows: ImmutableList<RankedBarUiModel>,
    val visibleCount: Int,
    val isLoading: Boolean = false,
    val areRowsTappable: Boolean = false,
) {
    companion object : UiModelPreviews<RankedBarsUiModel> {
        override val previews: ImmutableMap<String, RankedBarsUiModel> = persistentMapOf(
            "Filled" to RankedBarsUiModel(
                rows = persistentListOf(
                    RankedBarUiModel(
                        key = "fantasy",
                        label = "Fantasy",
                        value = "62%",
                        fill = 1f,
                    ),
                    RankedBarUiModel(
                        key = "science-fiction",
                        label = "Science fiction",
                        value = "41%",
                        fill = 0.66f,
                    ),
                    RankedBarUiModel(
                        key = "literary-fiction",
                        label = "Literary fiction",
                        value = "27%",
                        fill = 0.44f,
                    ),
                ),
                visibleCount = 3,
            ),
            "Tappable" to RankedBarsUiModel(
                rows = persistentListOf(
                    RankedBarUiModel(
                        key = "cozy",
                        label = "Cozy",
                        value = "9",
                        fill = 1f,
                    ),
                    RankedBarUiModel(
                        key = "reread",
                        label = "Reread",
                        value = "6",
                        fill = 0.66f,
                    ),
                    RankedBarUiModel(
                        key = "book-club",
                        label = "Book club",
                        value = "4",
                        fill = 0.44f,
                    ),
                ),
                visibleCount = 3,
                areRowsTappable = true,
            ),
            "Loading" to loading(rowCount = 3),
        )

        /** [rowCount] masked rows, all visible. */
        fun loading(rowCount: Int): RankedBarsUiModel = RankedBarsUiModel(
            rows = List(rowCount) { index ->
                RankedBarUiModel(
                    key = "loading-$index",
                    label = "",
                    value = "",
                    fill = 0f,
                )
            }.toImmutableList(),
            visibleCount = rowCount,
            isLoading = true,
        )
    }
}

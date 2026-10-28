package nl.rhaydus.softcover.core.component.chart

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * The last 84 days as small squares, oldest first, filled column by column so today ends bottom-right.
 * A null entry is a day without reading; a read day is tinted by its [YearGridStep].
 *
 * While [isLoading] the grid keeps its layout and masks.
 */
@Immutable
data class RecentDaysGridUiModel(
    val days: ImmutableList<YearGridStep?>,
    val contentDescription: String,
    val isLoading: Boolean = false,
) {
    companion object : UiModelPreviews<RecentDaysGridUiModel> {
        const val DAY_COUNT = 84
        private const val SKIP_STRIDE = 5
        private const val SPARSE_STRIDE = 9
        private const val DESCRIPTION = "Reading over the last 12 weeks"

        override val previews: ImmutableMap<String, RecentDaysGridUiModel> = persistentMapOf(
            "Loaded" to fixture(
                step = { index ->
                    if (index % SKIP_STRIDE == 0) null else YearGridStep.entries[(index * 7) % YearGridStep.entries.size]
                },
            ),
            "Sparse" to fixture(
                step = { index ->
                    if (index % SPARSE_STRIDE == 0) YearGridStep.entries[index % YearGridStep.entries.size] else null
                },
            ),
            "Loading" to fixture(
                step = { null },
                isLoading = true,
            ),
        )

        private fun fixture(
            step: (Int) -> YearGridStep?,
            isLoading: Boolean = false,
        ): RecentDaysGridUiModel = RecentDaysGridUiModel(
            days = List(
                size = DAY_COUNT,
                init = step,
            ).toImmutableList(),
            contentDescription = DESCRIPTION,
            isLoading = isLoading,
        )
    }
}

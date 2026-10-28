package nl.rhaydus.softcover.core.component.chart

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * A year of reading as one small square per day, a row per month, each read day tinted by its [YearGridStep].
 * [fewerLabel] and [moreLabel] end the key under the grid. Tapping a read day emits
 * [YearGridEvent.DayTapped]; empty days do nothing.
 *
 * While [isLoading] the grid keeps its layout and masks.
 */
@Immutable
data class YearGridUiModel(
    val year: Int,
    val months: ImmutableList<YearGridMonthUiModel>,
    val fewerLabel: String,
    val moreLabel: String,
    val isLoading: Boolean = false,
) {
    companion object : UiModelPreviews<YearGridUiModel> {
        private const val FIXTURE_YEAR = 2025
        private const val MONTH_COUNT = 12
        private const val STEP_COUNT = 5
        private const val SPARSE_STRIDE = 17
        private val MONTH_LABELS = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

        override val previews: ImmutableMap<String, YearGridUiModel> = persistentMapOf(
            "Loaded year" to fixture(isRead = { date -> (date.day * 7) % STEP_COUNT != 0 }),
            "Sparse year" to fixture(isRead = { date -> (date.month.ordinal * 31 + date.day) % SPARSE_STRIDE == 0 }),
            "Loading" to fixture(
                isRead = { false },
                isLoading = true,
            ),
        )

        private fun fixture(
            isRead: (LocalDate) -> Boolean,
            isLoading: Boolean = false,
        ): YearGridUiModel = YearGridUiModel(
            year = FIXTURE_YEAR,
            months = List(MONTH_COUNT) { monthIndex ->
                val first = LocalDate(
                    year = FIXTURE_YEAR,
                    month = monthIndex + 1,
                    day = 1,
                )
                val length = first
                    .plus(
                        value = 1,
                        unit = DateTimeUnit.MONTH,
                    )
                    .minus(
                        value = 1,
                        unit = DateTimeUnit.DAY,
                    )
                    .day

                YearGridMonthUiModel(
                    label = MONTH_LABELS[monthIndex],
                    days = List(length) { dayIndex ->
                        val date = LocalDate(
                            year = FIXTURE_YEAR,
                            month = monthIndex + 1,
                            day = dayIndex + 1,
                        )

                        if (isRead(date)) {
                            YearGridCell.Read(
                                date = date,
                                step = YearGridStep.entries[(date.day * 7 + monthIndex) % YearGridStep.entries.size],
                                contentDescription = "${MONTH_LABELS[monthIndex]} ${date.day}, pages read",
                            )
                        } else {
                            YearGridCell.Empty
                        }
                    }.toImmutableList(),
                )
            }.toImmutableList(),
            fewerLabel = "Fewer pages",
            moreLabel = "More",
            isLoading = isLoading,
        )
    }
}

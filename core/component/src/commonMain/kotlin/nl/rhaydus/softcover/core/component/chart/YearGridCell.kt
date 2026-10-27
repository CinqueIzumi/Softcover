package nl.rhaydus.softcover.core.component.chart

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate

/** One calendar day of a [YearGridMonthUiModel]; a day with no reading, or not yet come, is [Empty]. */
@Immutable
sealed interface YearGridCell {
    data object Empty : YearGridCell

    data class Read(
        val date: LocalDate,
        val step: YearGridStep,
        val contentDescription: String,
    ) : YearGridCell
}

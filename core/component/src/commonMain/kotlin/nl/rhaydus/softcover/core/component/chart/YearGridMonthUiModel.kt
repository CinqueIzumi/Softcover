package nl.rhaydus.softcover.core.component.chart

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

/** [days] holds one cell for every calendar day of the month, in order. */
@Immutable
data class YearGridMonthUiModel(
    val label: String,
    val days: ImmutableList<YearGridCell>,
)

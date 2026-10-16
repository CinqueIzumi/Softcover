package nl.rhaydus.softcover.feature.widget.presentation.mapper

import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod

internal fun trendingPeriodChipsFor(selected: TrendingPeriod): ChipSet<TrendingPeriod> = listOf(
    TrendingPeriod.Week,
    TrendingPeriod.Month,
    TrendingPeriod.ThreeMonths,
    TrendingPeriod.Year,
    TrendingPeriod.AllTime,
).map { it.label() to it }.toChipSet(selected = selected)

internal fun TrendingPeriod.label(): String = when (this) {
    TrendingPeriod.Week -> "This week"
    TrendingPeriod.Month -> "This month"
    TrendingPeriod.ThreeMonths -> "Past 3 months"
    TrendingPeriod.Year -> "Past year"
    TrendingPeriod.AllTime -> "All time"
}

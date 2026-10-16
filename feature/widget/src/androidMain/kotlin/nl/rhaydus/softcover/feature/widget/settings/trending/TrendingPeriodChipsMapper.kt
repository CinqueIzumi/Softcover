package nl.rhaydus.softcover.feature.widget.settings.trending

import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod

internal fun trendingPeriodChipsFor(selected: TrendingPeriod): ChipSet<TrendingPeriod> {
    val choices = listOf(
        TrendingPeriod.Week,
        TrendingPeriod.Month,
        TrendingPeriod.ThreeMonths,
        TrendingPeriod.Year,
        TrendingPeriod.AllTime,
    ).map { it.label() to it }

    return ChipSet(
        chips = choices.mapIndexed { index, (label, period) ->
            ChipUiModel(
                key = "$index",
                label = label,
                tone = ChipTone.Choice,
                selected = period == selected,
            )
        }.toImmutableList(),
        payloadByKey = choices.mapIndexed { index, (_, period) -> "$index" to period }.toMap().toImmutableMap(),
    )
}

internal fun TrendingPeriod.label(): String = when (this) {
    TrendingPeriod.Week -> "This week"
    TrendingPeriod.Month -> "This month"
    TrendingPeriod.ThreeMonths -> "Past 3 months"
    TrendingPeriod.Year -> "Past year"
    TrendingPeriod.AllTime -> "All time"
}

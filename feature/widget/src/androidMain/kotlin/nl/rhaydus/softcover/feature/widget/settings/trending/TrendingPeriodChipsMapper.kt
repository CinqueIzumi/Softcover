package nl.rhaydus.softcover.feature.widget.settings.trending

import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod

internal fun trendingPeriodChipsFor(selected: TrendingPeriod): ChipSet<TrendingPeriod> {
    val choices = listOf(
        "This week" to TrendingPeriod.Week,
        "This month" to TrendingPeriod.Month,
        "Past 3 months" to TrendingPeriod.ThreeMonths,
        "Past year" to TrendingPeriod.Year,
        "All time" to TrendingPeriod.AllTime,
    )

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

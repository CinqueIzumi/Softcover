package nl.rhaydus.softcover.feature.widget.settings.trending

import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.feature.widget.trending.TrendingRefreshCadence

internal fun trendingCadenceChipsFor(selected: TrendingRefreshCadence): ChipSet<TrendingRefreshCadence> {
    val choices = listOf(
        "Daily" to TrendingRefreshCadence.Daily,
        "Twice daily" to TrendingRefreshCadence.TwiceDaily,
    )

    return ChipSet(
        chips = choices.mapIndexed { index, (label, cadence) ->
            ChipUiModel(
                key = "$index",
                label = label,
                tone = ChipTone.Choice,
                selected = cadence == selected,
            )
        }.toImmutableList(),
        payloadByKey = choices.mapIndexed { index, (_, cadence) -> "$index" to cadence }.toMap().toImmutableMap(),
    )
}

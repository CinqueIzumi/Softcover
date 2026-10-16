package nl.rhaydus.softcover.feature.widget.presentation.mapper

import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingRefreshCadence

internal fun trendingCadenceChipsFor(selected: TrendingRefreshCadence): ChipSet<TrendingRefreshCadence> {
    val choices = listOf(
        TrendingRefreshCadence.Daily,
        TrendingRefreshCadence.TwiceDaily,
    ).map { it.label() to it }

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

internal fun TrendingRefreshCadence.label(): String = when (this) {
    TrendingRefreshCadence.Daily -> "Daily"
    TrendingRefreshCadence.TwiceDaily -> "Twice daily"
}

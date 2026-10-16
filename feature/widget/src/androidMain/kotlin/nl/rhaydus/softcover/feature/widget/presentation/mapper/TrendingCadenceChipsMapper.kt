package nl.rhaydus.softcover.feature.widget.presentation.mapper

import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingRefreshCadence

internal fun trendingCadenceChipsFor(selected: TrendingRefreshCadence): ChipSet<TrendingRefreshCadence> = listOf(
    TrendingRefreshCadence.Daily,
    TrendingRefreshCadence.TwiceDaily,
).map { it.label() to it }.toChipSet(selected = selected)

internal fun TrendingRefreshCadence.label(): String = when (this) {
    TrendingRefreshCadence.Daily -> "Daily"
    TrendingRefreshCadence.TwiceDaily -> "Twice daily"
}

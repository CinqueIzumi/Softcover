package nl.rhaydus.softcover.feature.widget.trending

import kotlin.time.Duration
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod

/** What a refresh run covers: the shortest interval any placed widget wants and every period one of them shows. */
internal data class TrendingRefreshPlan(
    val interval: Duration,
    val periods: Set<TrendingPeriod>,
)

internal fun trendingRefreshPlan(placed: List<TrendingSettings>): TrendingRefreshPlan? {
    val interval = resolveTrendingRefreshInterval(placed.map { it.cadence }) ?: return null

    return TrendingRefreshPlan(
        interval = interval,
        periods = placed.map { it.period }.toSet(),
    )
}

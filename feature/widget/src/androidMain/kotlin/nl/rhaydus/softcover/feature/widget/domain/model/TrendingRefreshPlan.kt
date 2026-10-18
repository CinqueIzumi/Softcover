package nl.rhaydus.softcover.feature.widget.domain.model

import kotlin.time.Duration

/** What a refresh run covers: the shortest interval any placed widget wants and every feed one of them shows. */
internal data class TrendingRefreshPlan(
    val interval: Duration,
    val feeds: Set<TrendingFeed>,
)

internal fun trendingRefreshPlan(placed: List<TrendingSettings>): TrendingRefreshPlan? {
    val interval = resolveTrendingRefreshInterval(placed.map { it.cadence }) ?: return null

    return TrendingRefreshPlan(
        interval = interval,
        feeds = placed.map { it.feed }.toSet(),
    )
}

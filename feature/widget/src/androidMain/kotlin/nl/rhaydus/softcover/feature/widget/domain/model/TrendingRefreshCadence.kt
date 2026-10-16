package nl.rhaydus.softcover.feature.widget.domain.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

internal enum class TrendingRefreshCadence(val interval: Duration) {
    Daily(24.hours),
    TwiceDaily(12.hours),
}

internal fun resolveTrendingRefreshInterval(cadences: List<TrendingRefreshCadence>): Duration? =
    cadences.minOfOrNull { it.interval }

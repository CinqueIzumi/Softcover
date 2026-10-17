package nl.rhaydus.softcover.feature.widget.domain.model

import nl.rhaydus.softcover.core.domain.model.TrendingPeriod

internal data class TrendingSettings(
    val period: TrendingPeriod = TrendingPeriod.Week,
    val cadence: TrendingRefreshCadence = TrendingRefreshCadence.Daily,
)

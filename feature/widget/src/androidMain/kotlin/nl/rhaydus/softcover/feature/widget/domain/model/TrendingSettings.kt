package nl.rhaydus.softcover.feature.widget.domain.model

internal data class TrendingSettings(
    val feed: TrendingFeed = TrendingFeed.Default,
    val cadence: TrendingRefreshCadence = TrendingRefreshCadence.Daily,
)

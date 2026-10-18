package nl.rhaydus.softcover.feature.widget.domain.model

internal enum class TrendingFeedKind {
    Trending,
    Upcoming,
}

internal val TrendingFeed.kind: TrendingFeedKind
    get() = when (this) {
        is TrendingFeed.Trending -> TrendingFeedKind.Trending
        is TrendingFeed.Upcoming -> TrendingFeedKind.Upcoming
    }

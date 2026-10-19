package nl.rhaydus.softcover.feature.widget.domain.model

internal enum class TrendingFeedKind {
    Trending,
    Recent,
    Upcoming,
}

internal val TrendingFeed.kind: TrendingFeedKind
    get() = when (this) {
        is TrendingFeed.Trending -> TrendingFeedKind.Trending
        is TrendingFeed.Recent -> TrendingFeedKind.Recent
        is TrendingFeed.Upcoming -> TrendingFeedKind.Upcoming
    }

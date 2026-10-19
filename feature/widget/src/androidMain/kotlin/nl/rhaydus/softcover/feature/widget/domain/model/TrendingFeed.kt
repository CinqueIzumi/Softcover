package nl.rhaydus.softcover.feature.widget.domain.model

import nl.rhaydus.softcover.core.domain.model.RecentWindow
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.softcover.core.domain.model.UpcomingWindow

internal sealed interface TrendingFeed {
    data class Trending(val period: TrendingPeriod) : TrendingFeed

    data class Recent(val window: RecentWindow) : TrendingFeed

    data class Upcoming(val window: UpcomingWindow) : TrendingFeed

    companion object {
        val Default: TrendingFeed = Trending(period = TrendingPeriod.Week)

        val All: List<TrendingFeed> = TrendingPeriod.entries.map { Trending(period = it) } +
            RecentWindow.entries.map { Recent(window = it) } +
            UpcomingWindow.entries.map { Upcoming(window = it) }
    }
}

package nl.rhaydus.softcover.feature.widget.domain.model

import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.softcover.core.domain.model.UpcomingWindow

/** What a Trending widget instance lists: what is trending over a period, or what releases within a window. */
internal sealed interface TrendingFeed {
    data class Trending(val period: TrendingPeriod) : TrendingFeed

    data class Upcoming(val window: UpcomingWindow) : TrendingFeed

    companion object {
        val Default: TrendingFeed = Trending(period = TrendingPeriod.Week)

        val All: List<TrendingFeed> = TrendingPeriod.entries.map { Trending(period = it) } +
            UpcomingWindow.entries.map { Upcoming(window = it) }
    }
}

package nl.rhaydus.softcover.feature.widget.presentation.state

import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.softcover.core.domain.model.UpcomingWindow
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFeed
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFeedKind
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingRefreshCadence
import nl.rhaydus.toad.LocalVariables

/** [lastTrending] and [lastUpcoming] remember each kind's choice, so flipping Show and back restores it. */
internal data class TrendingSettingsLocalVariables(
    val appWidgetId: Int? = null,
    val kind: TrendingFeedKind = TrendingFeedKind.Trending,
    val lastTrending: TrendingFeed.Trending = TrendingFeed.Trending(period = TrendingPeriod.Week),
    val lastUpcoming: TrendingFeed.Upcoming = TrendingFeed.Upcoming(window = UpcomingWindow.Month),
    val cadence: TrendingRefreshCadence = TrendingRefreshCadence.Daily,
) : LocalVariables {
    val feed: TrendingFeed
        get() = when (kind) {
            TrendingFeedKind.Trending -> lastTrending
            TrendingFeedKind.Upcoming -> lastUpcoming
        }
}

internal fun TrendingSettingsLocalVariables.withFeed(feed: TrendingFeed): TrendingSettingsLocalVariables =
    when (feed) {
        is TrendingFeed.Trending -> copy(
            kind = TrendingFeedKind.Trending,
            lastTrending = feed,
        )

        is TrendingFeed.Upcoming -> copy(
            kind = TrendingFeedKind.Upcoming,
            lastUpcoming = feed,
        )
    }

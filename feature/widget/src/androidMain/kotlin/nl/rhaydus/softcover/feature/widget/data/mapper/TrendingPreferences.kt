package nl.rhaydus.softcover.feature.widget.data.mapper

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import nl.rhaydus.softcover.core.domain.model.RecentWindow
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.softcover.core.domain.model.UpcomingWindow
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFeed
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingPosition
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingRefreshCadence
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingSettings

private val CadenceKey = stringPreferencesKey("trending_refresh_cadence")
private val PeriodKey = stringPreferencesKey("trending_period")
private val UpcomingWindowKey = stringPreferencesKey("trending_upcoming_window")
private val RecentWindowKey = stringPreferencesKey("trending_recent_window")
private val PageKey = intPreferencesKey("trending_page")

internal fun Preferences.toTrendingRefreshCadence(): TrendingRefreshCadence =
    TrendingRefreshCadence.entries.firstOrNull { it.name == this[CadenceKey] } ?: TrendingRefreshCadence.Daily

internal fun MutablePreferences.write(cadence: TrendingRefreshCadence) {
    this[CadenceKey] = cadence.name
}

/** An instance saved before the recent and upcoming feeds has no window key, so it reads back as trending. */
internal fun Preferences.toTrendingFeed(): TrendingFeed {
    val window = UpcomingWindow.entries.firstOrNull { it.storedName() == this[UpcomingWindowKey] }

    if (window != null) return TrendingFeed.Upcoming(window = window)

    val recentWindow = RecentWindow.entries.firstOrNull { it.storedName() == this[RecentWindowKey] }

    if (recentWindow != null) return TrendingFeed.Recent(window = recentWindow)

    return TrendingFeed.Trending(
        period = TrendingPeriod.entries.firstOrNull { it.storedName() == this[PeriodKey] } ?: TrendingPeriod.Week,
    )
}

internal fun MutablePreferences.write(feed: TrendingFeed) {
    when (feed) {
        is TrendingFeed.Trending -> {
            this[PeriodKey] = feed.period.storedName()
            remove(RecentWindowKey)
            remove(UpcomingWindowKey)
        }

        is TrendingFeed.Recent -> {
            this[RecentWindowKey] = feed.window.storedName()
            remove(UpcomingWindowKey)
        }

        is TrendingFeed.Upcoming -> {
            this[UpcomingWindowKey] = feed.window.storedName()
            remove(RecentWindowKey)
        }
    }
}

internal fun Preferences.toTrendingPage(): Int = this[PageKey] ?: 0

internal fun MutablePreferences.writeTrendingPage(page: Int) {
    this[PageKey] = page
}

internal fun Preferences.toTrendingSettings(): TrendingSettings = TrendingSettings(
    feed = toTrendingFeed(),
    cadence = toTrendingRefreshCadence(),
)

internal fun MutablePreferences.write(settings: TrendingSettings) {
    write(settings.feed)
    write(settings.cadence)
}

internal fun Preferences.toTrendingPosition(): TrendingPosition = TrendingPosition(
    feed = toTrendingFeed(),
    page = toTrendingPage(),
)

/** Stored by literal so reordering or renaming the enum cannot silently change a placed widget's period. */
private fun TrendingPeriod.storedName(): String = when (this) {
    TrendingPeriod.Week -> "week"
    TrendingPeriod.Month -> "month"
    TrendingPeriod.ThreeMonths -> "three_months"
    TrendingPeriod.Year -> "year"
    TrendingPeriod.AllTime -> "all_time"
}

private fun RecentWindow.storedName(): String = when (this) {
    RecentWindow.Week -> "week"
    RecentWindow.Month -> "month"
    RecentWindow.ThreeMonths -> "three_months"
    RecentWindow.SixMonths -> "six_months"
}

private fun UpcomingWindow.storedName(): String = when (this) {
    UpcomingWindow.Month -> "month"
    UpcomingWindow.ThreeMonths -> "three_months"
    UpcomingWindow.SixMonths -> "six_months"
    UpcomingWindow.Year -> "year"
}

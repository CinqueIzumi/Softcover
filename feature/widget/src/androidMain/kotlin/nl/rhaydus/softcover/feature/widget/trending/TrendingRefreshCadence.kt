package nl.rhaydus.softcover.feature.widget.trending

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

internal enum class TrendingRefreshCadence(val interval: Duration) {
    Daily(24.hours),
    TwiceDaily(12.hours),
}

private val CadenceKey = stringPreferencesKey("trending_refresh_cadence")

internal fun Preferences.toTrendingRefreshCadence(): TrendingRefreshCadence =
    TrendingRefreshCadence.entries.firstOrNull { it.name == this[CadenceKey] } ?: TrendingRefreshCadence.Daily

internal fun MutablePreferences.write(cadence: TrendingRefreshCadence) {
    this[CadenceKey] = cadence.name
}

internal fun resolveTrendingRefreshInterval(cadences: List<TrendingRefreshCadence>): Duration? =
    cadences.minOfOrNull { it.interval }

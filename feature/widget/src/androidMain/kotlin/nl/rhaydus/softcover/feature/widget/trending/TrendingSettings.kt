package nl.rhaydus.softcover.feature.widget.trending

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod

internal data class TrendingSettings(
    val period: TrendingPeriod = TrendingPeriod.Week,
    val cadence: TrendingRefreshCadence = TrendingRefreshCadence.Daily,
)

internal fun Preferences.toTrendingSettings(): TrendingSettings = TrendingSettings(
    period = toTrendingPeriod(),
    cadence = toTrendingRefreshCadence(),
)

internal fun MutablePreferences.write(settings: TrendingSettings) {
    write(settings.period)
    write(settings.cadence)
}

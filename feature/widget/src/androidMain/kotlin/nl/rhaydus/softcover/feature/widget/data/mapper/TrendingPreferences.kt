package nl.rhaydus.softcover.feature.widget.data.mapper

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingPosition
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingRefreshCadence
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingSettings

private val CadenceKey = stringPreferencesKey("trending_refresh_cadence")
private val PeriodKey = stringPreferencesKey("trending_period")
private val PageKey = intPreferencesKey("trending_page")

internal fun Preferences.toTrendingRefreshCadence(): TrendingRefreshCadence =
    TrendingRefreshCadence.entries.firstOrNull { it.name == this[CadenceKey] } ?: TrendingRefreshCadence.Daily

internal fun MutablePreferences.write(cadence: TrendingRefreshCadence) {
    this[CadenceKey] = cadence.name
}

internal fun Preferences.toTrendingPeriod(): TrendingPeriod =
    TrendingPeriod.entries.firstOrNull { it.storedName() == this[PeriodKey] } ?: TrendingPeriod.Week

internal fun MutablePreferences.write(period: TrendingPeriod) {
    this[PeriodKey] = period.storedName()
}

internal fun Preferences.toTrendingPage(): Int = this[PageKey] ?: 0

internal fun MutablePreferences.writeTrendingPage(page: Int) {
    this[PageKey] = page
}

internal fun Preferences.toTrendingSettings(): TrendingSettings = TrendingSettings(
    period = toTrendingPeriod(),
    cadence = toTrendingRefreshCadence(),
)

internal fun MutablePreferences.write(settings: TrendingSettings) {
    write(settings.period)
    write(settings.cadence)
}

internal fun Preferences.toTrendingPosition(): TrendingPosition = TrendingPosition(
    period = toTrendingPeriod(),
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

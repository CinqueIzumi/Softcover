package nl.rhaydus.softcover.feature.widget.trending

import androidx.datastore.preferences.core.Preferences
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod

/** Which period an instance shows and the page it is on; the page may be stale against a shorter list. */
internal data class TrendingPosition(
    val period: TrendingPeriod,
    val page: Int,
)

internal fun Preferences.toTrendingPosition(): TrendingPosition = TrendingPosition(
    period = toTrendingPeriod(),
    page = toTrendingPage(),
)

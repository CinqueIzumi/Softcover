package nl.rhaydus.softcover.feature.widget.settings.trending

import nl.rhaydus.softcover.feature.widget.trending.TrendingRefreshCadence
import nl.rhaydus.toad.LocalVariables

internal data class TrendingSettingsLocalVariables(
    val appWidgetId: Int? = null,
    val cadence: TrendingRefreshCadence = TrendingRefreshCadence.Daily,
) : LocalVariables

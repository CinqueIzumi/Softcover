package nl.rhaydus.softcover.feature.widget.presentation.state

import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingRefreshCadence
import nl.rhaydus.toad.LocalVariables

internal data class TrendingSettingsLocalVariables(
    val appWidgetId: Int? = null,
    val period: TrendingPeriod = TrendingPeriod.Week,
    val cadence: TrendingRefreshCadence = TrendingRefreshCadence.Daily,
) : LocalVariables

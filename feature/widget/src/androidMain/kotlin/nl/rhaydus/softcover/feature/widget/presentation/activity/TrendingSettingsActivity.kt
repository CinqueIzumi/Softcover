package nl.rhaydus.softcover.feature.widget.presentation.activity

import cafe.adriel.voyager.core.screen.Screen
import nl.rhaydus.softcover.feature.widget.presentation.glance.TrendingWidgetReceiver
import nl.rhaydus.softcover.feature.widget.presentation.screen.TrendingSettingsScreen

internal class TrendingSettingsActivity : WidgetSettingsActivity(TrendingWidgetReceiver::class.java) {
    override fun screen(appWidgetId: Int): Screen = TrendingSettingsScreen(appWidgetId = appWidgetId)
}

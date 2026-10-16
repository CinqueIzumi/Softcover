package nl.rhaydus.softcover.feature.widget.settings

import cafe.adriel.voyager.core.screen.Screen
import nl.rhaydus.softcover.feature.widget.settings.trending.TrendingSettingsScreen
import nl.rhaydus.softcover.feature.widget.trending.TrendingWidgetReceiver

internal class TrendingSettingsActivity : WidgetSettingsActivity(TrendingWidgetReceiver::class.java) {
    override fun screen(appWidgetId: Int): Screen = TrendingSettingsScreen(appWidgetId = appWidgetId)
}

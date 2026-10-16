package nl.rhaydus.softcover.feature.widget.presentation.activity

import cafe.adriel.voyager.core.screen.Screen
import nl.rhaydus.softcover.feature.widget.presentation.glance.RandomPickWidgetReceiver
import nl.rhaydus.softcover.feature.widget.presentation.screen.RandomPickSettingsScreen

internal class RandomPickSettingsActivity : WidgetSettingsActivity(RandomPickWidgetReceiver::class.java) {
    override fun screen(appWidgetId: Int): Screen = RandomPickSettingsScreen(appWidgetId = appWidgetId)
}

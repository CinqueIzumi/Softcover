package nl.rhaydus.softcover.feature.widget.settings

import cafe.adriel.voyager.core.screen.Screen
import nl.rhaydus.softcover.feature.widget.randompick.RandomPickWidgetReceiver
import nl.rhaydus.softcover.feature.widget.settings.randompick.RandomPickSettingsScreen

internal class RandomPickSettingsActivity : WidgetSettingsActivity(RandomPickWidgetReceiver::class.java) {
    override fun screen(
        appWidgetId: Int,
        onFinished: () -> Unit,
    ): Screen = RandomPickSettingsScreen(
        appWidgetId = appWidgetId,
        onFinished = onFinished,
    )
}

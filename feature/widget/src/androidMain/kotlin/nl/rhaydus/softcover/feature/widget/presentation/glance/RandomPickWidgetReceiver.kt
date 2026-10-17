package nl.rhaydus.softcover.feature.widget.presentation.glance

import androidx.glance.appwidget.GlanceAppWidget

internal class RandomPickWidgetReceiver : RefreshingWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = RandomPickWidget()
}

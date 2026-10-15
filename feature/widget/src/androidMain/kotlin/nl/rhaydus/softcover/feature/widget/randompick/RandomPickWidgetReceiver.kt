package nl.rhaydus.softcover.feature.widget.randompick

import androidx.glance.appwidget.GlanceAppWidget
import nl.rhaydus.softcover.feature.widget.refresh.RefreshingWidgetReceiver

internal class RandomPickWidgetReceiver : RefreshingWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = RandomPickWidget()
}

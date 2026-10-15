package nl.rhaydus.softcover.feature.widget

import androidx.glance.appwidget.GlanceAppWidget
import nl.rhaydus.softcover.feature.widget.refresh.RefreshingWidgetReceiver

internal class SoftcoverWidgetReceiver : RefreshingWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SoftcoverWidget()
}

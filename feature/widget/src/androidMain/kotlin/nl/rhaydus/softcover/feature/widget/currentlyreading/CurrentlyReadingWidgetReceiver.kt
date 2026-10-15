package nl.rhaydus.softcover.feature.widget.currentlyreading

import androidx.glance.appwidget.GlanceAppWidget
import nl.rhaydus.softcover.feature.widget.refresh.RefreshingWidgetReceiver

internal class CurrentlyReadingWidgetReceiver : RefreshingWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CurrentlyReadingWidget()
}

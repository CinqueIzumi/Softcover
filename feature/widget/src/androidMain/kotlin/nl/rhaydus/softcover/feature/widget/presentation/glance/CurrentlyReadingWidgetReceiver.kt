package nl.rhaydus.softcover.feature.widget.presentation.glance

import androidx.glance.appwidget.GlanceAppWidget

internal class CurrentlyReadingWidgetReceiver : RefreshingWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CurrentlyReadingWidget()
}

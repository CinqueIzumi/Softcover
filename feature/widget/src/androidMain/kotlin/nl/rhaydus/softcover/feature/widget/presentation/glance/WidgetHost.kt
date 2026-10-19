package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.os.Bundle
import androidx.glance.appwidget.GlanceAppWidget

internal interface WidgetHost {
    suspend fun placedAppWidgetIds(widget: GlanceAppWidget): List<Int>

    fun options(appWidgetId: Int): Bundle

    suspend fun <T> push(
        widget: GlanceAppWidget,
        source: WidgetModelSource<T>,
        appWidgetId: Int,
        model: T,
        options: Bundle,
    )
}

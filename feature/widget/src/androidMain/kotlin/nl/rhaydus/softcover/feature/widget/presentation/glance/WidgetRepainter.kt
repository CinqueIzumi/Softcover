package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll

/**
 * Repaints an instance that has no live Glance session. A live session does not re-run `provideGlance` on
 * `update`, so it picks up the write through the repository's change flow instead.
 */
internal class WidgetRepainter(context: Context) {
    private val context: Context = context.applicationContext

    suspend fun repaint(
        widget: GlanceAppWidget,
        appWidgetId: Int,
    ) {
        widget.update(
            context = context,
            id = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId),
        )
    }

    suspend fun repaintAll(widget: GlanceAppWidget) {
        widget.updateAll(context)
    }

    /** Repaints [appWidgetId] only if [widget] is placed on a home screen, and returns whether it did. */
    suspend fun repaintIfPlaced(
        widget: GlanceAppWidget,
        appWidgetId: Int,
    ): Boolean {
        val manager = GlanceAppWidgetManager(context)

        val glanceId = manager.getGlanceIds(widget.javaClass)
            .firstOrNull { manager.getAppWidgetId(it) == appWidgetId }
            ?: return false

        widget.update(
            context = context,
            id = glanceId,
        )

        return true
    }
}

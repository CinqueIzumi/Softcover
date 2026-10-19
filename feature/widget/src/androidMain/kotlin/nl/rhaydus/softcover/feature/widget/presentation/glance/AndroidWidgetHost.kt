package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Bundle
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.compose
import androidx.glance.appwidget.provideContent

internal class AndroidWidgetHost(
    context: Context,
) : WidgetHost {
    private val context: Context = context.applicationContext

    override suspend fun placedAppWidgetIds(widget: GlanceAppWidget): List<Int> {
        val manager = GlanceAppWidgetManager(context)

        return manager.getGlanceIds(widget.javaClass).map { manager.getAppWidgetId(it) }
    }

    override fun options(appWidgetId: Int): Bundle =
        AppWidgetManager.getInstance(context).getAppWidgetOptions(appWidgetId)

    override suspend fun <T> push(
        widget: GlanceAppWidget,
        source: WidgetModelSource<T>,
        appWidgetId: Int,
        model: T,
        options: Bundle,
    ) {
        val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)

        val views = snapshotOf(
            widget = widget,
            source = source,
            appWidgetId = appWidgetId,
            model = model,
        ).compose(
            context = context,
            id = glanceId,
            options = options,
        )

        AppWidgetManager.getInstance(context).updateAppWidget(
            appWidgetId,
            views,
        )
    }

    private fun <T> snapshotOf(
        widget: GlanceAppWidget,
        source: WidgetModelSource<T>,
        appWidgetId: Int,
        model: T,
    ): GlanceAppWidget = object : GlanceAppWidget() {
        override val sizeMode = widget.sizeMode

        override suspend fun provideGlance(
            context: Context,
            id: GlanceId,
        ) {
            provideContent {
                source.Content(
                    appWidgetId = appWidgetId,
                    model = model,
                )
            }
        }
    }
}

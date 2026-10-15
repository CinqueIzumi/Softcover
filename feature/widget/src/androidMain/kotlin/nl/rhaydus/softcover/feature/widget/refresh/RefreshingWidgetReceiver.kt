package nl.rhaydus.softcover.feature.widget.refresh

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import nl.rhaydus.softcover.feature.widget.worker.WidgetMaintenanceScheduler
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

internal abstract class RefreshingWidgetReceiver : GlanceAppWidgetReceiver(), KoinComponent {
    override fun onEnabled(context: Context) {
        super.onEnabled(context)

        get<WidgetRefresher>().onEnabled(widget = glanceAppWidget)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(
            context,
            appWidgetManager,
            appWidgetIds,
        )

        get<WidgetMaintenanceScheduler>().schedule()
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)

        get<WidgetRefresher>().onDisabled(widget = glanceAppWidget)

        get<WidgetMaintenanceScheduler>().requestCancelIfUnused()
    }
}

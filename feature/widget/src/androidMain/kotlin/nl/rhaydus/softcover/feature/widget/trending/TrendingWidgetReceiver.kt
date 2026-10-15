package nl.rhaydus.softcover.feature.widget.trending

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import nl.rhaydus.softcover.feature.widget.refresh.RefreshingWidgetReceiver
import nl.rhaydus.softcover.feature.widget.worker.TrendingRefreshScheduler
import org.koin.core.component.get

internal class TrendingWidgetReceiver : RefreshingWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TrendingWidget()

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

        rescheduleRefresh()
    }

    override fun onDeleted(
        context: Context,
        appWidgetIds: IntArray,
    ) {
        super.onDeleted(
            context,
            appWidgetIds,
        )

        rescheduleRefresh()
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)

        rescheduleRefresh()
    }

    private fun rescheduleRefresh() {
        get<TrendingRefreshScheduler>().requestReschedule()
    }
}

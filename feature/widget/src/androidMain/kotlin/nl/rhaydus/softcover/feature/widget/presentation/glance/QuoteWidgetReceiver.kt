package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import nl.rhaydus.softcover.feature.widget.presentation.worker.QuoteRefreshScheduler
import org.koin.core.component.get

internal class QuoteWidgetReceiver : RefreshingWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuoteWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)

        get<QuoteRefreshScheduler>().refreshNow()
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

        get<QuoteRefreshScheduler>().scheduleDaily()
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)

        get<QuoteRefreshScheduler>().cancel()
    }
}

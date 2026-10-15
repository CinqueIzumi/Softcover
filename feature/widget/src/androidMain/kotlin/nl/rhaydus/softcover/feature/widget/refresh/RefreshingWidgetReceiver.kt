package nl.rhaydus.softcover.feature.widget.refresh

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

internal abstract class RefreshingWidgetReceiver : GlanceAppWidgetReceiver(), KoinComponent {
    override fun onEnabled(context: Context) {
        super.onEnabled(context)

        get<WidgetRefresher>().onEnabled(widget = glanceAppWidget)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)

        get<WidgetRefresher>().onDisabled(widget = glanceAppWidget)
    }
}

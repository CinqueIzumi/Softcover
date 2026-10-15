package nl.rhaydus.softcover.feature.widget.trending

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition

internal class TrendingStore(private val context: Context) {
    suspend fun cadence(appWidgetId: Int): TrendingRefreshCadence = getAppWidgetState(
        context = context,
        definition = PreferencesGlanceStateDefinition,
        glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId),
    ).toTrendingRefreshCadence()

    suspend fun saveCadence(
        appWidgetId: Int,
        cadence: TrendingRefreshCadence,
    ) {
        val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)

        updateAppWidgetState(
            context = context,
            glanceId = glanceId,
        ) { preferences -> preferences.write(cadence) }
    }

    suspend fun placedCadences(): List<TrendingRefreshCadence> {
        val manager = GlanceAppWidgetManager(context)

        return manager.getGlanceIds(TrendingWidget::class.java).map { glanceId ->
            getAppWidgetState(
                context = context,
                definition = PreferencesGlanceStateDefinition,
                glanceId = glanceId,
            ).toTrendingRefreshCadence()
        }
    }
}

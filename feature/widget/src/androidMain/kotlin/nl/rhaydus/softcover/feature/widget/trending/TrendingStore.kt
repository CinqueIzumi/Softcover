package nl.rhaydus.softcover.feature.widget.trending

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

internal class TrendingStore(private val context: Context) {
    /** Emits the instance whose page or settings changed; a live Glance session does not re-run `provideGlance` on `update`. */
    val changes = MutableSharedFlow<GlanceId>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    suspend fun settings(appWidgetId: Int): TrendingSettings = getAppWidgetState(
        context = context,
        definition = PreferencesGlanceStateDefinition,
        glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId),
    ).toTrendingSettings()

    suspend fun position(glanceId: GlanceId): TrendingPosition = getAppWidgetState(
        context = context,
        definition = PreferencesGlanceStateDefinition,
        glanceId = glanceId,
    ).toTrendingPosition()

    suspend fun saveSettings(
        appWidgetId: Int,
        settings: TrendingSettings,
    ) {
        val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)

        updateAppWidgetState(
            context = context,
            glanceId = glanceId,
        ) { preferences ->
            if (preferences.toTrendingSettings() != settings) {
                preferences.write(settings)
                preferences.writeTrendingPage(0)
            }
        }

        changes.tryEmit(glanceId)

        TrendingWidget().update(
            context = context,
            id = glanceId,
        )
    }

    suspend fun placedSettings(): List<TrendingSettings> {
        val manager = GlanceAppWidgetManager(context)

        return manager.getGlanceIds(TrendingWidget::class.java).map { glanceId ->
            getAppWidgetState(
                context = context,
                definition = PreferencesGlanceStateDefinition,
                glanceId = glanceId,
            ).toTrendingSettings()
        }
    }
}

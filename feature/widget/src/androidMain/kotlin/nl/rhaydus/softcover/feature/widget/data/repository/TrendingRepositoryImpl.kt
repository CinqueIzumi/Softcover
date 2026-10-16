package nl.rhaydus.softcover.feature.widget.data.repository

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import nl.rhaydus.softcover.feature.widget.data.mapper.toTrendingPage
import nl.rhaydus.softcover.feature.widget.data.mapper.toTrendingPosition
import nl.rhaydus.softcover.feature.widget.data.mapper.toTrendingSettings
import nl.rhaydus.softcover.feature.widget.data.mapper.write
import nl.rhaydus.softcover.feature.widget.data.mapper.writeTrendingPage
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingPosition
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingSettings
import nl.rhaydus.softcover.feature.widget.domain.repository.TrendingRepository
import nl.rhaydus.softcover.feature.widget.domain.util.nextPage

internal class TrendingRepositoryImpl(private val context: Context) : TrendingRepository {
    override val changes = MutableSharedFlow<Int>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override suspend fun getSettings(appWidgetId: Int): TrendingSettings = getAppWidgetState(
        context = context,
        definition = PreferencesGlanceStateDefinition,
        glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId),
    ).toTrendingSettings()

    override suspend fun getPosition(appWidgetId: Int): TrendingPosition = getAppWidgetState(
        context = context,
        definition = PreferencesGlanceStateDefinition,
        glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId),
    ).toTrendingPosition()

    override suspend fun saveSettings(
        appWidgetId: Int,
        settings: TrendingSettings,
    ) {
        updateAppWidgetState(
            context = context,
            glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId),
        ) { preferences ->
            if (preferences.toTrendingSettings() != settings) {
                preferences.write(settings)
                preferences.writeTrendingPage(0)
            }
        }

        changes.tryEmit(appWidgetId)
    }

    override suspend fun advancePage(
        appWidgetId: Int,
        pageCount: Int,
        step: Int,
    ) {
        updateAppWidgetState(
            context = context,
            glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId),
        ) { preferences ->
            preferences.writeTrendingPage(
                nextPage(
                    stored = preferences.toTrendingPage(),
                    pageCount = pageCount,
                    step = step,
                ),
            )
        }

        changes.tryEmit(appWidgetId)
    }
}

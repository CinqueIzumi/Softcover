package nl.rhaydus.softcover.feature.widget.data.repository

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import nl.rhaydus.softcover.feature.widget.data.mapper.toQuoteSelection
import nl.rhaydus.softcover.feature.widget.data.mapper.write
import nl.rhaydus.softcover.feature.widget.domain.model.QuoteSelection
import nl.rhaydus.softcover.feature.widget.domain.repository.QuoteWidgetRepository

internal class QuoteWidgetRepositoryImpl(private val context: Context) : QuoteWidgetRepository {
    override val selectionChanges = MutableSharedFlow<Int>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override suspend fun getSelection(appWidgetId: Int): QuoteSelection? = getAppWidgetState(
        context = context,
        definition = PreferencesGlanceStateDefinition,
        glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId),
    ).toQuoteSelection()

    override suspend fun saveSelection(
        appWidgetId: Int,
        selection: QuoteSelection,
    ) {
        updateAppWidgetState(
            context = context,
            glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId),
        ) { preferences -> preferences.write(selection) }

        selectionChanges.tryEmit(appWidgetId)
    }
}

package nl.rhaydus.softcover.feature.widget.data.repository

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import nl.rhaydus.softcover.feature.widget.data.mapper.toSelectedBookId
import nl.rhaydus.softcover.feature.widget.data.mapper.writeSelectedBookId
import nl.rhaydus.softcover.feature.widget.domain.repository.CurrentlyReadingRepository
import nl.rhaydus.softcover.feature.widget.domain.util.nextBookId

internal class CurrentlyReadingRepositoryImpl(private val context: Context) : CurrentlyReadingRepository {
    override val selectionChanges = MutableSharedFlow<Int>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override suspend fun getSelectedBookId(appWidgetId: Int): Int? = getAppWidgetState(
        context = context,
        definition = PreferencesGlanceStateDefinition,
        glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId),
    ).toSelectedBookId()

    override suspend fun cycleSelectedBook(
        appWidgetId: Int,
        orderedBookIds: List<Int>,
        step: Int,
    ) {
        updateAppWidgetState(
            context = context,
            glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId),
        ) { preferences ->
            nextBookId(
                storedBookId = preferences.toSelectedBookId(),
                orderedBookIds = orderedBookIds,
                step = step,
            )?.let { preferences.writeSelectedBookId(it) }
        }

        selectionChanges.tryEmit(appWidgetId)
    }
}

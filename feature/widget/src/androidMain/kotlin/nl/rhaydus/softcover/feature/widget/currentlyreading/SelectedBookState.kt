package nl.rhaydus.softcover.feature.widget.currentlyreading

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition

internal val SelectedBookKey = intPreferencesKey("selected_book_id")

internal suspend fun Context.readSelectedBookId(glanceId: GlanceId): Int? = getAppWidgetState(
    context = this,
    definition = PreferencesGlanceStateDefinition,
    glanceId = glanceId,
)[SelectedBookKey]

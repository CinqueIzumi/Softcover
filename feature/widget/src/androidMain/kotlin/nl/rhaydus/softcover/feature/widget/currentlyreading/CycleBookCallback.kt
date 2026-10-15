package nl.rhaydus.softcover.feature.widget.currentlyreading

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import kotlinx.coroutines.flow.firstOrNull
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

internal class CycleBookCallback : ActionCallback, KoinComponent {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val step = parameters[StepKey] ?: return
        val dependencies = get<CurrentlyReadingWidgetDependencies>()

        val books = dependencies.getCurrentlyReadingUserBooks().firstOrNull() ?: return
        val orderedBookIds = books.map { it.id }

        updateAppWidgetState(
            context = context,
            glanceId = glanceId,
        ) { preferences ->
            nextBookId(
                storedBookId = preferences[SelectedBookKey],
                orderedBookIds = orderedBookIds,
                step = step,
            )?.let { preferences[SelectedBookKey] = it }
        }

        dependencies.selectionChanges.tryEmit(glanceId)

        CurrentlyReadingWidget().update(
            context = context,
            id = glanceId,
        )
    }

    companion object {
        val StepKey = ActionParameters.Key<Int>("step")
    }
}

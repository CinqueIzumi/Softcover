package nl.rhaydus.softcover.feature.widget.presentation.callback

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import kotlinx.coroutines.flow.firstOrNull
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.feature.widget.presentation.glance.CurrentlyReadingWidget
import nl.rhaydus.softcover.feature.widget.presentation.glance.CurrentlyReadingWidgetDependencies
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
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(glanceId)

        val books = dependencies.getCurrentlyReadingUserBooks().firstOrNull() ?: return

        dependencies.cycleSelectedBook(
            appWidgetId = appWidgetId,
            orderedBookIds = books.map { it.id },
            step = step,
        ).onSuccess {
            dependencies.repainter.repaint(
                widget = CurrentlyReadingWidget(),
                appWidgetId = appWidgetId,
            )
        }.onFailure { error ->
            AppLog.e(
                error,
                "Cycling the shown book failed",
            )
        }
    }

    companion object {
        val StepKey = ActionParameters.Key<Int>("step")
    }
}

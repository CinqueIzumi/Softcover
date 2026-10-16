package nl.rhaydus.softcover.feature.widget.presentation.callback

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import kotlinx.coroutines.flow.firstOrNull
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.feature.widget.domain.util.currentEpochDay
import nl.rhaydus.softcover.feature.widget.presentation.glance.QuoteWidget
import nl.rhaydus.softcover.feature.widget.presentation.glance.QuoteWidgetDependencies
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

internal class CycleQuoteCallback : ActionCallback, KoinComponent {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val step = parameters[StepKey] ?: return
        val dependencies = get<QuoteWidgetDependencies>()
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(glanceId)

        val quotes = dependencies.observeAllQuotes().firstOrNull() ?: return

        dependencies.cycleQuote(
            appWidgetId = appWidgetId,
            quotes = quotes,
            today = currentEpochDay(),
            step = step,
        ).onSuccess {
            dependencies.repainter.repaint(
                widget = QuoteWidget(),
                appWidgetId = appWidgetId,
            )
        }.onFailure { error ->
            AppLog.e(
                error,
                "Cycling the shown quote failed",
            )
        }
    }

    companion object {
        val StepKey = ActionParameters.Key<Int>("step")
    }
}

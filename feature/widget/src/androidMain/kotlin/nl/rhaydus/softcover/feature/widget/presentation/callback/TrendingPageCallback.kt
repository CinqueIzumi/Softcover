package nl.rhaydus.softcover.feature.widget.presentation.callback

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import kotlinx.coroutines.flow.firstOrNull
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.feature.widget.domain.util.pageCount
import nl.rhaydus.softcover.feature.widget.presentation.glance.TrendingWidget
import nl.rhaydus.softcover.feature.widget.presentation.glance.TrendingWidgetDependencies
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

internal class TrendingPageCallback : ActionCallback, KoinComponent {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val step = parameters[StepKey] ?: return
        val dependencies = get<TrendingWidgetDependencies>()
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(glanceId)

        val period = dependencies.getTrendingPosition(appWidgetId = appWidgetId)
            .onFailure { error ->
                AppLog.e(
                    error,
                    "Reading the trending period failed",
                )
            }
            .getOrNull()
            ?.period
            ?: return

        val books = dependencies.observeTrendingBooks(period = period).firstOrNull() ?: return

        dependencies.advanceTrendingPage(
            appWidgetId = appWidgetId,
            pageCount = pageCount(books.size),
            step = step,
        ).onSuccess {
            dependencies.repainter.repaint(
                widget = TrendingWidget(),
                appWidgetId = appWidgetId,
            )
        }.onFailure { error ->
            AppLog.e(
                error,
                "Advancing the trending page failed",
            )
        }
    }

    companion object {
        val StepKey = ActionParameters.Key<Int>("step")
    }
}

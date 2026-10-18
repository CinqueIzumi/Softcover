package nl.rhaydus.softcover.feature.widget.presentation.callback

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.feature.widget.presentation.glance.TrendingWidgetDependencies
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.time.Duration

/** Refetches the feed this widget shows. The fetch marks the feed running at once, which swaps the failure for the loader. */
internal class TrendingRetryCallback : ActionCallback, KoinComponent {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val dependencies = get<TrendingWidgetDependencies>()
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(glanceId)

        val feed = dependencies.getTrendingPosition(appWidgetId = appWidgetId)
            .onFailure { error ->
                AppLog.e(
                    error,
                    "Reading the trending feed to retry failed",
                )
            }
            .getOrNull()
            ?.feed
            ?: return

        dependencies.fetcher.fetch(
            feeds = listOf(feed),
            maxAge = Duration.ZERO,
        )
    }
}

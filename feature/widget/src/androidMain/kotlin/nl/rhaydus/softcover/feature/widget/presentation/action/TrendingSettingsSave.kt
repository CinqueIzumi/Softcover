package nl.rhaydus.softcover.feature.widget.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.common.runCatchingCancellable
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingSettings
import nl.rhaydus.softcover.feature.widget.presentation.glance.TrendingWidget
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.TrendingSettingsDependencies

internal suspend fun TrendingSettingsDependencies.saveSettings(
    appWidgetId: Int?,
    settings: TrendingSettings,
) {
    if (appWidgetId == null) return

    saveTrendingSettings(
        appWidgetId = appWidgetId,
        settings = settings,
    ).onSuccess {
        repainter.repaint(
            widget = TrendingWidget(),
            appWidgetId = appWidgetId,
        )

        fetcher.fetch(
            feeds = listOf(settings.feed),
            maxAge = settings.cadence.interval / 2,
        )
    }.onFailure { error ->
        AppLog.e(
            error,
            "Saving trending settings failed",
        )
    }

    runCatchingCancellable { scheduler.reschedule() }
        .onFailure { error ->
            AppLog.e(
                error,
                "Rescheduling trending refresh failed",
            )
        }
}

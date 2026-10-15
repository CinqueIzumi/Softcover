package nl.rhaydus.softcover.feature.widget.settings.trending

import nl.rhaydus.common.AppLog
import nl.rhaydus.common.runCatchingCancellable
import nl.rhaydus.softcover.feature.widget.trending.TrendingRefreshCadence

internal suspend fun TrendingSettingsDependencies.saveCadence(
    appWidgetId: Int?,
    cadence: TrendingRefreshCadence,
) {
    if (appWidgetId == null) return

    runCatchingCancellable {
        store.saveCadence(
            appWidgetId = appWidgetId,
            cadence = cadence,
        )

        scheduler.reschedule()
    }.onFailure { error ->
        AppLog.e(
            error,
            "Saving trending settings failed",
        )
    }
}

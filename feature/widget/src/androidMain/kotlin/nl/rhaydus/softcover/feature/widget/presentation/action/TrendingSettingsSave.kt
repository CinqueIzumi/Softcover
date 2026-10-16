package nl.rhaydus.softcover.feature.widget.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.common.runCatchingCancellable
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingSettings
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.TrendingSettingsDependencies

internal suspend fun TrendingSettingsDependencies.saveSettings(
    appWidgetId: Int?,
    settings: TrendingSettings,
) {
    if (appWidgetId == null) return

    runCatchingCancellable {
        store.saveSettings(
            appWidgetId = appWidgetId,
            settings = settings,
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

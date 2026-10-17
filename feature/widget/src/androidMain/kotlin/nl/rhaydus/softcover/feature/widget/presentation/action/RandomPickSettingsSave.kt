package nl.rhaydus.softcover.feature.widget.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickSettings
import nl.rhaydus.softcover.feature.widget.presentation.glance.RandomPickWidget
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.RandomPickSettingsDependencies

internal suspend fun RandomPickSettingsDependencies.saveSettings(
    appWidgetId: Int?,
    settings: RandomPickSettings,
) {
    if (appWidgetId == null) return

    saveRandomPickSettings(
        appWidgetId = appWidgetId,
        settings = settings,
    ).onSuccess {
        repainter.repaint(
            widget = RandomPickWidget(),
            appWidgetId = appWidgetId,
        )
    }.onFailure { error ->
        AppLog.e(
            error,
            "Saving random pick settings failed",
        )
    }
}

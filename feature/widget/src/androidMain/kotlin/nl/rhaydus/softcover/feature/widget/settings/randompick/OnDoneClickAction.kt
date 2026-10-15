package nl.rhaydus.softcover.feature.widget.settings.randompick

import nl.rhaydus.common.AppLog
import nl.rhaydus.common.runCatchingCancellable
import nl.rhaydus.softcover.feature.widget.randompick.RandomPickSettings
import nl.rhaydus.toad.ActionScope

internal data object OnDoneClickAction : RandomPickSettingsAction {
    override suspend fun execute(
        dependencies: RandomPickSettingsDependencies,
        scope: ActionScope<RandomPickSettingsUiState, RandomPickSettingsEvent, RandomPickSettingsLocalVariables>,
    ) {
        val local = scope.currentLocalVariables
        val appWidgetId = local.appWidgetId

        if (appWidgetId != null) {
            runCatchingCancellable {
                dependencies.store.saveSettings(
                    appWidgetId = appWidgetId,
                    settings = RandomPickSettings(
                        source = local.source,
                        cadence = local.cadence,
                    ),
                )
            }.onFailure { error ->
                AppLog.e(
                    error,
                    "Saving random pick settings failed",
                )
            }
        }

        scope.sendEvent(event = RandomPickSettingsEvent.Finished)
    }
}

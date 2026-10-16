package nl.rhaydus.softcover.feature.widget.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.common.runCatchingCancellable
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickSettings
import nl.rhaydus.softcover.feature.widget.presentation.event.RandomPickSettingsEvent
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.RandomPickSettingsDependencies
import nl.rhaydus.softcover.feature.widget.presentation.state.RandomPickSettingsLocalVariables
import nl.rhaydus.softcover.feature.widget.presentation.state.RandomPickSettingsUiState
import nl.rhaydus.toad.ActionScope

internal data object OnRandomPickDoneClickAction : RandomPickSettingsAction {
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

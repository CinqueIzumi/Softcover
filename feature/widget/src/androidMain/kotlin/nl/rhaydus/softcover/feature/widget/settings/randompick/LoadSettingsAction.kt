package nl.rhaydus.softcover.feature.widget.settings.randompick

import nl.rhaydus.common.AppLog
import nl.rhaydus.common.runCatchingCancellable
import nl.rhaydus.toad.ActionScope

internal class LoadSettingsAction(private val appWidgetId: Int) : RandomPickSettingsAction {
    override suspend fun execute(
        dependencies: RandomPickSettingsDependencies,
        scope: ActionScope<RandomPickSettingsUiState, RandomPickSettingsEvent, RandomPickSettingsLocalVariables>,
    ) {
        if (scope.currentLocalVariables.appWidgetId != null) return

        runCatchingCancellable { dependencies.store.settings(appWidgetId = appWidgetId) }
            .onSuccess { settings ->
                scope.setLocalVariables {
                    it.copy(
                        appWidgetId = appWidgetId,
                        source = settings.source,
                        cadence = settings.cadence,
                    )
                }

                scope.setState {
                    it.copy(
                        sourceChips = sourceChipsFor(
                            selected = settings.source,
                            lists = scope.currentLocalVariables.lists,
                        ),
                        cadenceChips = cadenceChipsFor(selected = settings.cadence),
                    )
                }
            }
            .onFailure { error ->
                AppLog.e(
                    error,
                    "Loading random pick settings failed",
                )
            }
    }
}

package nl.rhaydus.softcover.feature.widget.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.common.runCatchingCancellable
import nl.rhaydus.softcover.feature.widget.presentation.event.RandomPickSettingsEvent
import nl.rhaydus.softcover.feature.widget.presentation.mapper.cadenceChipsFor
import nl.rhaydus.softcover.feature.widget.presentation.mapper.sourceChipsFor
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.RandomPickSettingsDependencies
import nl.rhaydus.softcover.feature.widget.presentation.state.RandomPickSettingsLocalVariables
import nl.rhaydus.softcover.feature.widget.presentation.state.RandomPickSettingsUiState
import nl.rhaydus.toad.ActionScope

internal class LoadRandomPickSettingsAction(private val appWidgetId: Int) : RandomPickSettingsAction {
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

package nl.rhaydus.softcover.feature.widget.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.common.runCatchingCancellable
import nl.rhaydus.softcover.feature.widget.presentation.event.RandomPickSettingsEvent
import nl.rhaydus.softcover.feature.widget.presentation.glance.RandomPickWidget
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

        dependencies.getRandomPickSettings(appWidgetId = appWidgetId)
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

                dependencies.launch {
                    dependencies.warmWidget(appWidgetId = appWidgetId)
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

/**
 * Starts the placed instance's Glance session so the later save reaches it through the change flow, not a cold
 * start. Best-effort: a session that idles out before the save falls back to the cold path. An instance that is
 * not placed yet has nothing to warm.
 */
private suspend fun RandomPickSettingsDependencies.warmWidget(appWidgetId: Int) {
    runCatchingCancellable {
        repainter.repaintIfPlaced(
            widget = RandomPickWidget(),
            appWidgetId = appWidgetId,
        )
    }.onFailure { error ->
        AppLog.e(
            error,
            "Warming the random pick widget failed",
        )
    }
}

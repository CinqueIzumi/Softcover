package nl.rhaydus.softcover.feature.widget.settings.trending

import nl.rhaydus.common.AppLog
import nl.rhaydus.common.runCatchingCancellable
import nl.rhaydus.toad.ActionScope

internal class LoadTrendingSettingsAction(private val appWidgetId: Int) : TrendingSettingsAction {
    override suspend fun execute(
        dependencies: TrendingSettingsDependencies,
        scope: ActionScope<TrendingSettingsUiState, TrendingSettingsEvent, TrendingSettingsLocalVariables>,
    ) {
        if (scope.currentLocalVariables.appWidgetId != null) return

        runCatchingCancellable { dependencies.store.cadence(appWidgetId = appWidgetId) }
            .onSuccess { cadence ->
                scope.setLocalVariables {
                    it.copy(
                        appWidgetId = appWidgetId,
                        cadence = cadence,
                    )
                }

                scope.setState { it.copy(cadenceChips = trendingCadenceChipsFor(selected = cadence)) }
            }
            .onFailure { error ->
                AppLog.e(
                    error,
                    "Loading trending settings failed",
                )
            }
    }
}

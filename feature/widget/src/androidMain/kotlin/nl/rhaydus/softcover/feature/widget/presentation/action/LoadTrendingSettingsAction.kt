package nl.rhaydus.softcover.feature.widget.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.feature.widget.presentation.event.TrendingSettingsEvent
import nl.rhaydus.softcover.feature.widget.presentation.mapper.trendingCadenceChipsFor
import nl.rhaydus.softcover.feature.widget.presentation.mapper.trendingPeriodChipsFor
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.TrendingSettingsDependencies
import nl.rhaydus.softcover.feature.widget.presentation.state.TrendingSettingsLocalVariables
import nl.rhaydus.softcover.feature.widget.presentation.state.TrendingSettingsUiState
import nl.rhaydus.toad.ActionScope

internal class LoadTrendingSettingsAction(private val appWidgetId: Int) : TrendingSettingsAction {
    override suspend fun execute(
        dependencies: TrendingSettingsDependencies,
        scope: ActionScope<TrendingSettingsUiState, TrendingSettingsEvent, TrendingSettingsLocalVariables>,
    ) {
        if (scope.currentLocalVariables.appWidgetId != null) return

        dependencies.getTrendingSettings(appWidgetId = appWidgetId)
            .onSuccess { settings ->
                scope.setLocalVariables {
                    it.copy(
                        appWidgetId = appWidgetId,
                        period = settings.period,
                        cadence = settings.cadence,
                    )
                }

                scope.setState {
                    it.copy(
                        periodChips = trendingPeriodChipsFor(selected = settings.period),
                        cadenceChips = trendingCadenceChipsFor(selected = settings.cadence),
                    )
                }
            }
            .onFailure { error ->
                AppLog.e(
                    error,
                    "Loading trending settings failed",
                )
            }
    }
}

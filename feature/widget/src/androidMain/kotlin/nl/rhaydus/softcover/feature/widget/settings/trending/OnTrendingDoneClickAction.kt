package nl.rhaydus.softcover.feature.widget.settings.trending

import nl.rhaydus.softcover.feature.widget.trending.TrendingSettings
import nl.rhaydus.toad.ActionScope

internal data object OnTrendingDoneClickAction : TrendingSettingsAction {
    override suspend fun execute(
        dependencies: TrendingSettingsDependencies,
        scope: ActionScope<TrendingSettingsUiState, TrendingSettingsEvent, TrendingSettingsLocalVariables>,
    ) {
        val local = scope.currentLocalVariables

        dependencies.saveSettings(
            appWidgetId = local.appWidgetId,
            settings = TrendingSettings(
                period = local.period,
                cadence = local.cadence,
            ),
        )

        scope.sendEvent(event = TrendingSettingsEvent.Finished)
    }
}

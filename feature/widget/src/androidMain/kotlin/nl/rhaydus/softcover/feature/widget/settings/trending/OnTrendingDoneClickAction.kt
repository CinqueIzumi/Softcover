package nl.rhaydus.softcover.feature.widget.settings.trending

import nl.rhaydus.toad.ActionScope

internal data object OnTrendingDoneClickAction : TrendingSettingsAction {
    override suspend fun execute(
        dependencies: TrendingSettingsDependencies,
        scope: ActionScope<TrendingSettingsUiState, TrendingSettingsEvent, TrendingSettingsLocalVariables>,
    ) {
        val local = scope.currentLocalVariables

        dependencies.saveCadence(
            appWidgetId = local.appWidgetId,
            cadence = local.cadence,
        )

        scope.sendEvent(event = TrendingSettingsEvent.Finished)
    }
}

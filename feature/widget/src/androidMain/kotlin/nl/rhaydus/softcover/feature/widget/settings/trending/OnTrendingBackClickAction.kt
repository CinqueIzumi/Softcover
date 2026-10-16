package nl.rhaydus.softcover.feature.widget.settings.trending

import nl.rhaydus.toad.ActionScope

internal data object OnTrendingBackClickAction : TrendingSettingsAction {
    override suspend fun execute(
        dependencies: TrendingSettingsDependencies,
        scope: ActionScope<TrendingSettingsUiState, TrendingSettingsEvent, TrendingSettingsLocalVariables>,
    ) {
        scope.sendEvent(event = TrendingSettingsEvent.Finished)
    }
}

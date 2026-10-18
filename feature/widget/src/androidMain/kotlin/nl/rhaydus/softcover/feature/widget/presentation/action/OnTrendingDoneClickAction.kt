package nl.rhaydus.softcover.feature.widget.presentation.action

import nl.rhaydus.softcover.feature.widget.domain.model.TrendingSettings
import nl.rhaydus.softcover.feature.widget.presentation.event.TrendingSettingsEvent
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.TrendingSettingsDependencies
import nl.rhaydus.softcover.feature.widget.presentation.state.TrendingSettingsLocalVariables
import nl.rhaydus.softcover.feature.widget.presentation.state.TrendingSettingsUiState
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
                feed = local.feed,
                cadence = local.cadence,
            ),
        )

        scope.sendEvent(event = TrendingSettingsEvent.Finished)
    }
}

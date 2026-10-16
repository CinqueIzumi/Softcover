package nl.rhaydus.softcover.feature.settings.presentation.action

import nl.rhaydus.softcover.feature.settings.presentation.event.WidgetsSettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.WidgetsSettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.WidgetsSettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.WidgetsSettingsUiState
import nl.rhaydus.toad.ActionScope

internal class OnWidgetsBackClickAction : WidgetsSettingsAction {
    override suspend fun execute(
        dependencies: WidgetsSettingsDependencies,
        scope: ActionScope<WidgetsSettingsUiState, WidgetsSettingsEvent, WidgetsSettingsLocalVariables>,
    ) {
        scope.sendEvent(event = WidgetsSettingsEvent.NavigateBack)
    }
}

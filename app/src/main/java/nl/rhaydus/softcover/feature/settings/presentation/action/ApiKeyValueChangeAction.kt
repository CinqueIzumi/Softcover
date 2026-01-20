package nl.rhaydus.softcover.feature.settings.presentation.action

import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.softcover.feature.settings.presentation.viewmodel.SettingsDependencies

data class ApiKeyValueChangeAction(val newValue: String) : SettingsAction {
    override suspend fun execute(
        dependencies: SettingsDependencies,
        scope: ActionScope<SettingsScreenUiState, SettingsEvent>,
    ) {
        scope.setState { copy(apiKey = newValue) }
    }
}
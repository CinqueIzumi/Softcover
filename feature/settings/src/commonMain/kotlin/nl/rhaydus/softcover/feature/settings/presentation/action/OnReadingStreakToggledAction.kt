package nl.rhaydus.softcover.feature.settings.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.toad.ActionScope

internal class OnReadingStreakToggledAction(val newValue: Boolean) : SettingsAction {
    override suspend fun execute(
        dependencies: SettingsScreenDependencies,
        scope: ActionScope<SettingsScreenUiState, SettingsScreenEvent, SettingsLocalVariables>,
    ) {
        dependencies.setReadingStreakEnabledUseCase(enabled = newValue).onFailure {
            AppLog.e("$it")
        }
    }
}

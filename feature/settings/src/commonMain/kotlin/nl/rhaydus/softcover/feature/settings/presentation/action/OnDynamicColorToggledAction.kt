package nl.rhaydus.softcover.feature.settings.presentation.action

import nl.rhaydus.softcover.core.domain.logging.AppLog
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.toad.ActionScope

internal class OnDynamicColorToggledAction(val newValue: Boolean) : SettingsAction {
    override suspend fun execute(
        dependencies: SettingsScreenDependencies,
        scope: ActionScope<SettingsScreenUiState, SettingsScreenEvent, SettingsLocalVariables>,
    ) {
        dependencies.setDynamicColorUseCase(enabled = newValue).onFailure {
            AppLog.e("$it")
        }
    }
}

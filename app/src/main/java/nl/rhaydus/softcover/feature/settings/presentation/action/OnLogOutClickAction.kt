package nl.rhaydus.softcover.feature.settings.presentation.action

import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.softcover.feature.settings.presentation.viewmodel.SettingsScreenDependencies
import timber.log.Timber

class OnLogOutClickAction : SettingsAction {
    override suspend fun execute(
        dependencies: SettingsScreenDependencies,
        scope: ActionScope<SettingsScreenUiState, SettingsScreenEvent, SettingsLocalVariables>,
    ) {
        dependencies.resetUserDataUseCase().onFailure {
            Timber.e("-=- $it")
        }
    }
}
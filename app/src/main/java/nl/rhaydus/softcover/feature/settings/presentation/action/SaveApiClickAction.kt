package nl.rhaydus.softcover.feature.settings.presentation.action

import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.presentation.util.SnackBarManager
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.softcover.feature.settings.presentation.viewmodel.SettingsDependencies

data object SaveApiKeyClickAction : SettingsAction {
    override suspend fun execute(
        dependencies: SettingsDependencies,
        scope: ActionScope<SettingsScreenUiState, SettingsEvent>,
    ) {
        val updatedKey = scope.currentState.apiKey
            .removePrefix("Bearer")
            .trim()

        dependencies.launch {
            dependencies.resetUserDataUseCase()

            dependencies.updateApiKeyUseCase(key = updatedKey).onSuccess {
                dependencies.initializeUserIdUseCase().onFailure {
                    SnackBarManager.showSnackbar(title = "Something went wrong while trying to initialize the user's profile.")
                }
            }
        }
    }
}
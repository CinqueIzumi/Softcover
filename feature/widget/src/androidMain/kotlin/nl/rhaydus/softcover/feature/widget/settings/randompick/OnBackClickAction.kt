package nl.rhaydus.softcover.feature.widget.settings.randompick

import nl.rhaydus.toad.ActionScope

internal data object OnBackClickAction : RandomPickSettingsAction {
    override suspend fun execute(
        dependencies: RandomPickSettingsDependencies,
        scope: ActionScope<RandomPickSettingsUiState, RandomPickSettingsEvent, RandomPickSettingsLocalVariables>,
    ) {
        scope.sendEvent(event = RandomPickSettingsEvent.Finished)
    }
}

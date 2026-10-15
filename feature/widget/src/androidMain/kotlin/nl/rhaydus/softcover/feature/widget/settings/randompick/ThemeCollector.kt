package nl.rhaydus.softcover.feature.widget.settings.randompick

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.toad.ActionScope

internal class ThemeCollector : RandomPickSettingsCollector {
    override suspend fun onLaunch(
        scope: ActionScope<RandomPickSettingsUiState, RandomPickSettingsEvent, RandomPickSettingsLocalVariables>,
        dependencies: RandomPickSettingsDependencies,
    ) {
        dependencies.getThemeConfiguration().collectLatest { configuration ->
            scope.setState { it.copy(themeConfiguration = configuration) }
        }
    }
}

package nl.rhaydus.softcover.feature.widget.settings.collector

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.feature.widget.settings.RandomPickSettingsDependencies
import nl.rhaydus.softcover.feature.widget.settings.RandomPickSettingsEvent
import nl.rhaydus.softcover.feature.widget.settings.RandomPickSettingsLocalVariables
import nl.rhaydus.softcover.feature.widget.settings.RandomPickSettingsUiState
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

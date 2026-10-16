package nl.rhaydus.softcover.feature.widget.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.feature.widget.presentation.event.RandomPickSettingsEvent
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.RandomPickSettingsDependencies
import nl.rhaydus.softcover.feature.widget.presentation.state.RandomPickSettingsLocalVariables
import nl.rhaydus.softcover.feature.widget.presentation.state.RandomPickSettingsUiState
import nl.rhaydus.toad.ActionScope

internal class RandomPickThemeCollector : RandomPickSettingsCollector {
    override suspend fun onLaunch(
        scope: ActionScope<RandomPickSettingsUiState, RandomPickSettingsEvent, RandomPickSettingsLocalVariables>,
        dependencies: RandomPickSettingsDependencies,
    ) {
        dependencies.getThemeConfiguration().collectLatest { configuration ->
            scope.setState { it.copy(themeConfiguration = configuration) }
        }
    }
}

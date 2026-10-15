package nl.rhaydus.softcover.feature.widget.settings.trending

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.toad.ActionScope

internal class TrendingThemeCollector : TrendingSettingsCollector {
    override suspend fun onLaunch(
        scope: ActionScope<TrendingSettingsUiState, TrendingSettingsEvent, TrendingSettingsLocalVariables>,
        dependencies: TrendingSettingsDependencies,
    ) {
        dependencies.getThemeConfiguration().collectLatest { configuration ->
            scope.setState { it.copy(themeConfiguration = configuration) }
        }
    }
}

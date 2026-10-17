package nl.rhaydus.softcover.feature.widget.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.feature.widget.presentation.event.TrendingSettingsEvent
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.TrendingSettingsDependencies
import nl.rhaydus.softcover.feature.widget.presentation.state.TrendingSettingsLocalVariables
import nl.rhaydus.softcover.feature.widget.presentation.state.TrendingSettingsUiState
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

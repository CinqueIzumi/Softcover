package nl.rhaydus.softcover.feature.settings.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.core.domain.model.BottomBarStyle
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.toad.ActionScope

internal class ThemeConfigurationCollector : SettingsCollector {
    override suspend fun onLaunch(
        scope: ActionScope<SettingsScreenUiState, SettingsScreenEvent, SettingsLocalVariables>,
        dependencies: SettingsScreenDependencies,
    ) {
        dependencies.getThemeConfigurationUseCase().collectLatest { configuration ->
            scope.setState {
                it.copy(
                    useFloatingBarChecked = configuration.bottomBarStyle == BottomBarStyle.FLOATING,
                    themeMode = configuration.themeMode,
                    useDynamicColorChecked = configuration.useDynamicColor,
                )
            }
        }
    }
}

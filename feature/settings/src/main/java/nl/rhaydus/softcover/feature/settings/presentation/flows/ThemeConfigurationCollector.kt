package nl.rhaydus.softcover.feature.settings.presentation.flows

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.domain.model.BottomBarStyle
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState

internal class ThemeConfigurationCollector : SettingsInitializer {
    override suspend fun onLaunch(
        scope: ActionScope<SettingsScreenUiState, SettingsScreenEvent, SettingsLocalVariables>,
        dependencies: SettingsScreenDependencies,
    ) {
        dependencies.getThemeConfigurationUseCase().collectLatest { configuration ->
            scope.setState {
                it.copy(
                    useFloatingBarChecked = configuration.bottomBarStyle == BottomBarStyle.FLOATING,
                    useDynamicColorChecked = configuration.useDynamicColor,
                )
            }
        }
    }
}
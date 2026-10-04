package nl.rhaydus.softcover.feature.settings.presentation.collector

import kotlinx.coroutines.flow.combine
import nl.rhaydus.softcover.core.domain.model.BottomBarStyle
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.mapper.displayToggleRowsFor
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.softcover.feature.settings.presentation.util.settingsDisplayToggleAvailability
import nl.rhaydus.toad.ActionScope

/**
 * Builds [SettingsScreenUiState.displayToggleRows] from the bottom-bar style, shelf-swipe and
 * reading-streak preferences, with the platform's [settingsDisplayToggleAvailability] deciding
 * which rows exist.
 */
internal class DisplayToggleRowsCollector : SettingsCollector {
    override suspend fun onLaunch(
        scope: ActionScope<SettingsScreenUiState, SettingsScreenEvent, SettingsLocalVariables>,
        dependencies: SettingsScreenDependencies,
    ) {
        val availability = settingsDisplayToggleAvailability()

        combine(
            dependencies.getThemeConfigurationUseCase(),
            dependencies.getShelfSwipeEnabledAsFlowUseCase(),
            dependencies.getReadingStreakEnabledAsFlowUseCase(),
        ) { configuration, shelfSwipeEnabled, readingStreakEnabled ->
            displayToggleRowsFor(
                showFloatingBarToggle = availability.showsFloatingBarToggle,
                showShelfSwipeToggle = availability.showsShelfSwipeToggle,
                useFloatingBarChecked = configuration.bottomBarStyle == BottomBarStyle.FLOATING,
                shelfSwipeEnabledChecked = shelfSwipeEnabled,
                readingStreakEnabledChecked = readingStreakEnabled,
            )
        }.collect { rows ->
            scope.setState { it.copy(displayToggleRows = rows) }
        }
    }
}

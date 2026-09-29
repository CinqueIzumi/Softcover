package nl.rhaydus.softcover.feature.settings.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.mapper.displayToggleRowsFor
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.toad.ActionScope

internal class ShelfSwipeCollector : SettingsCollector {
    override suspend fun onLaunch(
        scope: ActionScope<SettingsScreenUiState, SettingsScreenEvent, SettingsLocalVariables>,
        dependencies: SettingsScreenDependencies,
    ) {
        dependencies.getShelfSwipeEnabledAsFlowUseCase().collectLatest { enabled ->
            scope.setState {
                val next = it.copy(shelfSwipeEnabledChecked = enabled)

                next.copy(
                    displayToggleRows = displayToggleRowsFor(
                        showFloatingBarToggle = next.showFloatingBarToggle,
                        showShelfSwipeToggle = next.showShelfSwipeToggle,
                        useFloatingBarChecked = next.useFloatingBarChecked,
                        shelfSwipeEnabledChecked = next.shelfSwipeEnabledChecked,
                        readingStreakEnabledChecked = next.readingStreakEnabledChecked,
                    ),
                )
            }
        }
    }
}

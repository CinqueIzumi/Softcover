package nl.rhaydus.softcover.feature.widget.settings.collector

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.feature.widget.settings.RandomPickSettingsDependencies
import nl.rhaydus.softcover.feature.widget.settings.RandomPickSettingsEvent
import nl.rhaydus.softcover.feature.widget.settings.RandomPickSettingsLocalVariables
import nl.rhaydus.softcover.feature.widget.settings.RandomPickSettingsUiState
import nl.rhaydus.softcover.feature.widget.settings.mapper.sourceChipsFor
import nl.rhaydus.toad.ActionScope

internal class ListsCollector : RandomPickSettingsCollector {
    override suspend fun onLaunch(
        scope: ActionScope<RandomPickSettingsUiState, RandomPickSettingsEvent, RandomPickSettingsLocalVariables>,
        dependencies: RandomPickSettingsDependencies,
    ) {
        dependencies.getAllUserLists().collectLatest { lists ->
            scope.setLocalVariables { it.copy(lists = lists) }

            scope.setState {
                it.copy(
                    sourceChips = sourceChipsFor(
                        selected = scope.currentLocalVariables.source,
                        lists = lists,
                    ),
                )
            }
        }
    }
}

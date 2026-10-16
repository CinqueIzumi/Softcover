package nl.rhaydus.softcover.feature.widget.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.feature.widget.presentation.event.RandomPickSettingsEvent
import nl.rhaydus.softcover.feature.widget.presentation.mapper.sourceChipsFor
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.RandomPickSettingsDependencies
import nl.rhaydus.softcover.feature.widget.presentation.state.RandomPickSettingsLocalVariables
import nl.rhaydus.softcover.feature.widget.presentation.state.RandomPickSettingsUiState
import nl.rhaydus.toad.ActionScope

internal class RandomPickListsCollector : RandomPickSettingsCollector {
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

package nl.rhaydus.softcover.feature.widget.settings.action

import nl.rhaydus.softcover.feature.widget.randompick.RandomPickSource
import nl.rhaydus.softcover.feature.widget.settings.RandomPickSettingsDependencies
import nl.rhaydus.softcover.feature.widget.settings.RandomPickSettingsEvent
import nl.rhaydus.softcover.feature.widget.settings.RandomPickSettingsLocalVariables
import nl.rhaydus.softcover.feature.widget.settings.RandomPickSettingsUiState
import nl.rhaydus.softcover.feature.widget.settings.mapper.sourceChipsFor
import nl.rhaydus.toad.ActionScope

internal class OnSourceSelectedAction(private val source: RandomPickSource) : RandomPickSettingsAction {
    override suspend fun execute(
        dependencies: RandomPickSettingsDependencies,
        scope: ActionScope<RandomPickSettingsUiState, RandomPickSettingsEvent, RandomPickSettingsLocalVariables>,
    ) {
        scope.setLocalVariables { it.copy(source = source) }

        scope.setState {
            it.copy(
                sourceChips = sourceChipsFor(
                    selected = source,
                    lists = scope.currentLocalVariables.lists,
                ),
            )
        }
    }
}

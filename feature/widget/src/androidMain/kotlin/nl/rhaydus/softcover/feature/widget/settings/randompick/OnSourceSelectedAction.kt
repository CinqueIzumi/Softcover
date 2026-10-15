package nl.rhaydus.softcover.feature.widget.settings.randompick

import nl.rhaydus.softcover.feature.widget.randompick.RandomPickSource
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

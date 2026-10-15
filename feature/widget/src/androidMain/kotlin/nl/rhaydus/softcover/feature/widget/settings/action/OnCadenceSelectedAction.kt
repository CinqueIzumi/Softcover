package nl.rhaydus.softcover.feature.widget.settings.action

import nl.rhaydus.softcover.feature.widget.randompick.RandomPickCadence
import nl.rhaydus.softcover.feature.widget.settings.RandomPickSettingsDependencies
import nl.rhaydus.softcover.feature.widget.settings.RandomPickSettingsEvent
import nl.rhaydus.softcover.feature.widget.settings.RandomPickSettingsLocalVariables
import nl.rhaydus.softcover.feature.widget.settings.RandomPickSettingsUiState
import nl.rhaydus.softcover.feature.widget.settings.mapper.cadenceChipsFor
import nl.rhaydus.toad.ActionScope

internal class OnCadenceSelectedAction(private val cadence: RandomPickCadence) : RandomPickSettingsAction {
    override suspend fun execute(
        dependencies: RandomPickSettingsDependencies,
        scope: ActionScope<RandomPickSettingsUiState, RandomPickSettingsEvent, RandomPickSettingsLocalVariables>,
    ) {
        scope.setLocalVariables { it.copy(cadence = cadence) }

        scope.setState { it.copy(cadenceChips = cadenceChipsFor(selected = cadence)) }
    }
}

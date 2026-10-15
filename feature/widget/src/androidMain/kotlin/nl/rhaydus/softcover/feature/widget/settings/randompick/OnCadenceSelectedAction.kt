package nl.rhaydus.softcover.feature.widget.settings.randompick

import nl.rhaydus.softcover.feature.widget.randompick.RandomPickCadence
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

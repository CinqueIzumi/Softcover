package nl.rhaydus.softcover.feature.widget.presentation.action

import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickCadence
import nl.rhaydus.softcover.feature.widget.presentation.event.RandomPickSettingsEvent
import nl.rhaydus.softcover.feature.widget.presentation.mapper.cadenceChipsFor
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.RandomPickSettingsDependencies
import nl.rhaydus.softcover.feature.widget.presentation.state.RandomPickSettingsLocalVariables
import nl.rhaydus.softcover.feature.widget.presentation.state.RandomPickSettingsUiState
import nl.rhaydus.toad.ActionScope

internal class OnRandomPickCadenceSelectedAction(private val cadence: RandomPickCadence) : RandomPickSettingsAction {
    override suspend fun execute(
        dependencies: RandomPickSettingsDependencies,
        scope: ActionScope<RandomPickSettingsUiState, RandomPickSettingsEvent, RandomPickSettingsLocalVariables>,
    ) {
        scope.setLocalVariables { it.copy(cadence = cadence) }

        scope.setState { it.copy(cadenceChips = cadenceChipsFor(selected = cadence)) }
    }
}

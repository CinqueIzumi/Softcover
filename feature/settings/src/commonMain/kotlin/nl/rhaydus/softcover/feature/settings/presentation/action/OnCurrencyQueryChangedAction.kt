package nl.rhaydus.softcover.feature.settings.presentation.action

import nl.rhaydus.softcover.feature.settings.presentation.event.CurrencySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.CurrencySettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.CurrencySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.CurrencySettingsUiState
import nl.rhaydus.softcover.feature.settings.presentation.state.withRows
import nl.rhaydus.toad.ActionScope

internal class OnCurrencyQueryChangedAction(val query: String) : CurrencySettingsAction {
    override suspend fun execute(
        dependencies: CurrencySettingsDependencies,
        scope: ActionScope<CurrencySettingsUiState, CurrencySettingsEvent, CurrencySettingsLocalVariables>,
    ) {
        scope.setState { it.copy(query = query).withRows() }
    }
}

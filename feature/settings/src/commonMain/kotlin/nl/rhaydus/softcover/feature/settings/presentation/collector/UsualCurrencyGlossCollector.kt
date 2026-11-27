package nl.rhaydus.softcover.feature.settings.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.toad.ActionScope

internal class UsualCurrencyGlossCollector : SettingsCollector {
    override suspend fun onLaunch(
        scope: ActionScope<SettingsScreenUiState, SettingsScreenEvent, SettingsLocalVariables>,
        dependencies: SettingsScreenDependencies,
    ) {
        dependencies.getUsualCurrencyAsFlowUseCase().collectLatest { currency ->
            scope.setState {
                it.copy(currencyGloss = "${currency.name}, for the prices you note")
            }
        }
    }
}

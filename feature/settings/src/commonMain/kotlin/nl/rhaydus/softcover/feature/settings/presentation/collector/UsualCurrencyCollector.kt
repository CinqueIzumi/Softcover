package nl.rhaydus.softcover.feature.settings.presentation.collector

import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.feature.settings.presentation.event.CurrencySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.CurrencySettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.CurrencySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.CurrencySettingsUiState
import nl.rhaydus.softcover.feature.settings.presentation.state.withRows
import nl.rhaydus.toad.ActionScope

internal class UsualCurrencyCollector : CurrencySettingsCollector {
    override suspend fun onLaunch(
        scope: ActionScope<CurrencySettingsUiState, CurrencySettingsEvent, CurrencySettingsLocalVariables>,
        dependencies: CurrencySettingsDependencies,
    ) {
        val currencies = dependencies.getCurrenciesUseCase().toImmutableList()

        dependencies.getUsualCurrencyAsFlowUseCase().collectLatest { usual ->
            scope.setState {
                it.copy(
                    currencies = currencies,
                    selectedCode = usual.code,
                ).withRows()
            }
        }
    }
}

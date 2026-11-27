package nl.rhaydus.softcover.feature.settings.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetCurrenciesUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetUsualCurrencyAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetUsualCurrencyUseCase
import nl.rhaydus.softcover.feature.settings.presentation.action.CurrencySettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.collector.CurrencySettingsCollector
import nl.rhaydus.softcover.feature.settings.presentation.event.CurrencySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.state.CurrencySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.CurrencySettingsUiState
import nl.rhaydus.toad.ToadScreenModel

internal class CurrencySettingsScreenModel(
    getCurrenciesUseCase: GetCurrenciesUseCase,
    getUsualCurrencyAsFlowUseCase: GetUsualCurrencyAsFlowUseCase,
    setUsualCurrencyUseCase: SetUsualCurrencyUseCase,
    appDispatchers: AppDispatchers,
    flows: List<CurrencySettingsCollector>,
) : ToadScreenModel<
        CurrencySettingsUiState,
        CurrencySettingsEvent,
        CurrencySettingsDependencies,
        CurrencySettingsCollector,
        CurrencySettingsLocalVariables,
    >(
        initialState = CurrencySettingsUiState(),
        initialLocalVariables = CurrencySettingsLocalVariables(),
        initializers = flows,
    ) {
    override val dependencies = CurrencySettingsDependencies(
        getCurrenciesUseCase = getCurrenciesUseCase,
        getUsualCurrencyAsFlowUseCase = getUsualCurrencyAsFlowUseCase,
        setUsualCurrencyUseCase = setUsualCurrencyUseCase,
        mainDispatcher = appDispatchers.main,
        coroutineScope = screenModelScope,
    )

    init {
        startInitializers()
    }

    fun runAction(action: CurrencySettingsAction) = dispatch(action = action)
}

package nl.rhaydus.softcover.feature.widget.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.softcover.feature.widget.data.datastore.TrendingStore
import nl.rhaydus.softcover.feature.widget.presentation.action.TrendingSettingsAction
import nl.rhaydus.softcover.feature.widget.presentation.collector.TrendingSettingsCollector
import nl.rhaydus.softcover.feature.widget.presentation.event.TrendingSettingsEvent
import nl.rhaydus.softcover.feature.widget.presentation.state.TrendingSettingsLocalVariables
import nl.rhaydus.softcover.feature.widget.presentation.state.TrendingSettingsUiState
import nl.rhaydus.softcover.feature.widget.presentation.worker.TrendingRefreshScheduler
import nl.rhaydus.toad.ToadScreenModel

internal class TrendingSettingsScreenModel(
    store: TrendingStore,
    scheduler: TrendingRefreshScheduler,
    getThemeConfiguration: GetThemeConfigurationUseCase,
    appDispatchers: AppDispatchers,
    flows: List<TrendingSettingsCollector>,
) : ToadScreenModel<
    TrendingSettingsUiState,
    TrendingSettingsEvent,
    TrendingSettingsDependencies,
    TrendingSettingsCollector,
    TrendingSettingsLocalVariables,
    >(
    initialState = TrendingSettingsUiState(),
    initialLocalVariables = TrendingSettingsLocalVariables(),
    initializers = flows,
) {
    override val dependencies = TrendingSettingsDependencies(
        store = store,
        scheduler = scheduler,
        getThemeConfiguration = getThemeConfiguration,
        mainDispatcher = appDispatchers.main,
        coroutineScope = screenModelScope,
    )

    init {
        startInitializers()
    }

    fun runAction(action: TrendingSettingsAction) = dispatch(action = action)
}

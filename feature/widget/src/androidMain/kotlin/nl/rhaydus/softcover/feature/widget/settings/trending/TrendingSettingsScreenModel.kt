package nl.rhaydus.softcover.feature.widget.settings.trending

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.softcover.feature.widget.trending.TrendingStore
import nl.rhaydus.softcover.feature.widget.worker.TrendingRefreshScheduler
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

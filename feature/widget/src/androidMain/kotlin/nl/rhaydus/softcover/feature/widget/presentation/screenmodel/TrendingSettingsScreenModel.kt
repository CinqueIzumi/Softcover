package nl.rhaydus.softcover.feature.widget.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.GetTrendingSettingsUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.SaveTrendingSettingsUseCase
import nl.rhaydus.softcover.feature.widget.presentation.action.TrendingSettingsAction
import nl.rhaydus.softcover.feature.widget.presentation.collector.TrendingSettingsCollector
import nl.rhaydus.softcover.feature.widget.presentation.event.TrendingSettingsEvent
import nl.rhaydus.softcover.feature.widget.presentation.glance.WidgetRepainter
import nl.rhaydus.softcover.feature.widget.presentation.refresh.TrendingFeedFetcher
import nl.rhaydus.softcover.feature.widget.presentation.state.TrendingSettingsLocalVariables
import nl.rhaydus.softcover.feature.widget.presentation.state.TrendingSettingsUiState
import nl.rhaydus.softcover.feature.widget.presentation.worker.TrendingRefreshScheduler
import nl.rhaydus.toad.ToadScreenModel

internal class TrendingSettingsScreenModel(
    getTrendingSettings: GetTrendingSettingsUseCase,
    saveTrendingSettings: SaveTrendingSettingsUseCase,
    repainter: WidgetRepainter,
    scheduler: TrendingRefreshScheduler,
    fetcher: TrendingFeedFetcher,
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
        getTrendingSettings = getTrendingSettings,
        saveTrendingSettings = saveTrendingSettings,
        repainter = repainter,
        scheduler = scheduler,
        fetcher = fetcher,
        getThemeConfiguration = getThemeConfiguration,
        mainDispatcher = appDispatchers.main,
        coroutineScope = screenModelScope,
    )

    init {
        startInitializers()
    }

    fun runAction(action: TrendingSettingsAction) = dispatch(action = action)
}

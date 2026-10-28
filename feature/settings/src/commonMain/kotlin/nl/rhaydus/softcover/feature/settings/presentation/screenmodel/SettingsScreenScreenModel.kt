package nl.rhaydus.softcover.feature.settings.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.domain.app.AppVersionProvider
import nl.rhaydus.softcover.core.domain.appupdate.ObserveAppUpdateStateUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetDateStyleAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetReadingStreakEnabledAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetShelfSwipeEnabledAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetUiScaleAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetReadingStreakEnabledUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetShelfSwipeEnabledUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetUiScaleUseCase
import nl.rhaydus.softcover.core.presentation.widget.HomeScreenWidgets
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetBottomBarStyleUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetColorPaletteUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetDateStyleUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetDynamicColorUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetThemeModeUseCase
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.collector.SettingsCollector
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.toad.ToadScreenModel

internal class SettingsScreenScreenModel(
    getDateStyleAsFlowUseCase: GetDateStyleAsFlowUseCase,
    setDateStyleUseCase: SetDateStyleUseCase,
    setBottomBarStyleUseCase: SetBottomBarStyleUseCase,
    setThemeModeUseCase: SetThemeModeUseCase,
    setColorPaletteUseCase: SetColorPaletteUseCase,
    setDynamicColorUseCase: SetDynamicColorUseCase,
    getThemeConfigurationUseCase: GetThemeConfigurationUseCase,
    getReadingStreakEnabledAsFlowUseCase: GetReadingStreakEnabledAsFlowUseCase,
    setReadingStreakEnabledUseCase: SetReadingStreakEnabledUseCase,
    getShelfSwipeEnabledAsFlowUseCase: GetShelfSwipeEnabledAsFlowUseCase,
    setShelfSwipeEnabledUseCase: SetShelfSwipeEnabledUseCase,
    getUiScaleAsFlowUseCase: GetUiScaleAsFlowUseCase,
    setUiScaleUseCase: SetUiScaleUseCase,
    observeAppUpdateStateUseCase: ObserveAppUpdateStateUseCase,
    appVersionProvider: AppVersionProvider,
    homeScreenWidgets: HomeScreenWidgets,
    appDispatchers: AppDispatchers,
    flows: List<SettingsCollector>,
) : ToadScreenModel<SettingsScreenUiState, SettingsScreenEvent, SettingsScreenDependencies, SettingsCollector, SettingsLocalVariables>(
    initialState = SettingsScreenUiState(
        appVersionName = appVersionProvider.versionInfo.name,
        appVersionCode = appVersionProvider.versionInfo.code,
        widgetsSupported = homeScreenWidgets.isSupported,
    ),
    initialLocalVariables = SettingsLocalVariables(),
    initializers = flows,
) {
    override val dependencies = SettingsScreenDependencies(
        getThemeConfigurationUseCase = getThemeConfigurationUseCase,
        setBottomBarStyleUseCase = setBottomBarStyleUseCase,
        setThemeModeUseCase = setThemeModeUseCase,
        setColorPaletteUseCase = setColorPaletteUseCase,
        setDynamicColorUseCase = setDynamicColorUseCase,
        setDateStyleUseCase = setDateStyleUseCase,
        getDateStyleAsFlowUseCase = getDateStyleAsFlowUseCase,
        getReadingStreakEnabledAsFlowUseCase = getReadingStreakEnabledAsFlowUseCase,
        setReadingStreakEnabledUseCase = setReadingStreakEnabledUseCase,
        getShelfSwipeEnabledAsFlowUseCase = getShelfSwipeEnabledAsFlowUseCase,
        setShelfSwipeEnabledUseCase = setShelfSwipeEnabledUseCase,
        getUiScaleAsFlowUseCase = getUiScaleAsFlowUseCase,
        setUiScaleUseCase = setUiScaleUseCase,
        observeAppUpdateStateUseCase = observeAppUpdateStateUseCase,
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
    )

    init {
        startInitializers()
    }

    fun runAction(action: SettingsAction) = dispatch(action)
}

package nl.rhaydus.softcover.feature.settings.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.domain.app.AppVersionProvider
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetDateStyleAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetReadingStreakEnabledAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetUiScaleAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetReadingStreakEnabledUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetUiScaleUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetBottomBarStyleUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetDateStyleUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetDynamicColorUseCase
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.collector.SettingsCollector
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.toad.ToadScreenModel

internal class SettingsScreenScreenModel(
    private val getDateStyleAsFlowUseCase: GetDateStyleAsFlowUseCase,
    private val setDateStyleUseCase: SetDateStyleUseCase,
    private val setBottomBarStyleUseCase: SetBottomBarStyleUseCase,
    private val setDynamicColorUseCase: SetDynamicColorUseCase,
    private val getThemeConfigurationUseCase: GetThemeConfigurationUseCase,
    private val getReadingStreakEnabledAsFlowUseCase: GetReadingStreakEnabledAsFlowUseCase,
    private val setReadingStreakEnabledUseCase: SetReadingStreakEnabledUseCase,
    private val getUiScaleAsFlowUseCase: GetUiScaleAsFlowUseCase,
    private val setUiScaleUseCase: SetUiScaleUseCase,
    appVersionProvider: AppVersionProvider,
    appDispatchers: AppDispatchers,
    flows: List<SettingsCollector>,
) : ToadScreenModel<SettingsScreenUiState, SettingsScreenEvent, SettingsScreenDependencies, SettingsCollector, SettingsLocalVariables>(
    initialState = SettingsScreenUiState(
        appVersionName = appVersionProvider.versionInfo.name,
        appVersionCode = appVersionProvider.versionInfo.code,
        dateStyleExamples = DateStyle.entries.associateWith {
            it.format(Clock.System.todayIn(TimeZone.currentSystemDefault()))
        },
    ),
    initialLocalVariables = SettingsLocalVariables(),
    initializers = flows,
) {
    override val dependencies = SettingsScreenDependencies(
        getThemeConfigurationUseCase = getThemeConfigurationUseCase,
        setBottomBarStyleUseCase = setBottomBarStyleUseCase,
        setDynamicColorUseCase = setDynamicColorUseCase,
        setDateStyleUseCase = setDateStyleUseCase,
        getDateStyleAsFlowUseCase = getDateStyleAsFlowUseCase,
        getReadingStreakEnabledAsFlowUseCase = getReadingStreakEnabledAsFlowUseCase,
        setReadingStreakEnabledUseCase = setReadingStreakEnabledUseCase,
        getUiScaleAsFlowUseCase = getUiScaleAsFlowUseCase,
        setUiScaleUseCase = setUiScaleUseCase,
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
    )

    init {
        startInitializers()
    }

    fun runAction(action: SettingsAction) = dispatch(action)
}

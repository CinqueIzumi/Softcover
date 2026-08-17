package nl.rhaydus.softcover.feature.settings.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetDateStyleAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetReadingStreakEnabledAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetShelfSwipeEnabledAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetUiScaleAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetReadingStreakEnabledUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetShelfSwipeEnabledUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetUiScaleUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetBottomBarStyleUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetDateStyleUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetDynamicColorUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetThemeModeUseCase
import nl.rhaydus.toad.ActionDependencies

internal data class SettingsScreenDependencies(
    val getThemeConfigurationUseCase: GetThemeConfigurationUseCase,
    val setBottomBarStyleUseCase: SetBottomBarStyleUseCase,
    val setThemeModeUseCase: SetThemeModeUseCase,
    val setDynamicColorUseCase: SetDynamicColorUseCase,
    val setDateStyleUseCase: SetDateStyleUseCase,
    val getDateStyleAsFlowUseCase: GetDateStyleAsFlowUseCase,
    val getReadingStreakEnabledAsFlowUseCase: GetReadingStreakEnabledAsFlowUseCase,
    val setReadingStreakEnabledUseCase: SetReadingStreakEnabledUseCase,
    val getShelfSwipeEnabledAsFlowUseCase: GetShelfSwipeEnabledAsFlowUseCase,
    val setShelfSwipeEnabledUseCase: SetShelfSwipeEnabledUseCase,
    val getUiScaleAsFlowUseCase: GetUiScaleAsFlowUseCase,
    val setUiScaleUseCase: SetUiScaleUseCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()

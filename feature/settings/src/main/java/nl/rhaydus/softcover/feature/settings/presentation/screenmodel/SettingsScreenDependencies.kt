package nl.rhaydus.softcover.feature.settings.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionDependencies
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetDateStyleAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetBottomBarStyleUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetDateStyleUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetDynamicColorUseCase

internal data class SettingsScreenDependencies(
    val getThemeConfigurationUseCase: GetThemeConfigurationUseCase,
    val setBottomBarStyleUseCase: SetBottomBarStyleUseCase,
    val setDynamicColorUseCase: SetDynamicColorUseCase,
    val setDateStyleUseCase: SetDateStyleUseCase,
    val getDateStyleAsFlowUseCase: GetDateStyleAsFlowUseCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()
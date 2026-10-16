package nl.rhaydus.softcover.feature.widget.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.lists.domain.usecase.GetAllUserListsUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.GetRandomPickSettingsUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.SaveRandomPickSettingsUseCase
import nl.rhaydus.softcover.feature.widget.presentation.glance.WidgetRepainter
import nl.rhaydus.toad.ActionDependencies

internal class RandomPickSettingsDependencies(
    val getRandomPickSettings: GetRandomPickSettingsUseCase,
    val saveRandomPickSettings: SaveRandomPickSettingsUseCase,
    val repainter: WidgetRepainter,
    val getThemeConfiguration: GetThemeConfigurationUseCase,
    val getAllUserLists: GetAllUserListsUseCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()

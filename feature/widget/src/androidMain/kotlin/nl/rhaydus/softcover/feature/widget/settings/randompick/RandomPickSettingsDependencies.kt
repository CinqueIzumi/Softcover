package nl.rhaydus.softcover.feature.widget.settings.randompick

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.lists.domain.usecase.GetAllUserListsUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.softcover.feature.widget.randompick.RandomPickStore
import nl.rhaydus.toad.ActionDependencies

internal class RandomPickSettingsDependencies(
    val store: RandomPickStore,
    val getThemeConfiguration: GetThemeConfigurationUseCase,
    val getAllUserLists: GetAllUserListsUseCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()

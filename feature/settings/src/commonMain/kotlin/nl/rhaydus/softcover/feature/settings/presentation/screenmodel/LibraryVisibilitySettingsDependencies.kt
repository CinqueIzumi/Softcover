package nl.rhaydus.softcover.feature.settings.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.library.domain.usecase.RefreshLibraryUseCase
import nl.rhaydus.softcover.core.lists.domain.usecase.GetAllUserListsUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetEnabledListIdsAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetEnabledStatusCodesAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetLibraryTabOrderAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetEnabledListIdsUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetEnabledStatusCodesUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetLibraryTabOrderUseCase
import nl.rhaydus.toad.ActionDependencies

internal class LibraryVisibilitySettingsDependencies(
    val getEnabledStatusCodesAsFlowUseCase: GetEnabledStatusCodesAsFlowUseCase,
    val getEnabledListIdsAsFlowUseCase: GetEnabledListIdsAsFlowUseCase,
    val getLibraryTabOrderAsFlowUseCase: GetLibraryTabOrderAsFlowUseCase,
    val setEnabledStatusCodesUseCase: SetEnabledStatusCodesUseCase,
    val setEnabledListIdsUseCase: SetEnabledListIdsUseCase,
    val setLibraryTabOrderUseCase: SetLibraryTabOrderUseCase,
    val getAllUserListsUseCase: GetAllUserListsUseCase,
    val refreshLibraryUseCase: RefreshLibraryUseCase,
    val applicationScope: ApplicationScope,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()

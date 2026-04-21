package nl.rhaydus.softcover.feature.settings.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.presentation.toad.ActionDependencies
import nl.rhaydus.softcover.feature.books.domain.usecase.GetAllUserListsUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.RefreshUserBooksUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetEnabledListIdsAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetEnabledStatusCodesAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetEnabledListIdsUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetEnabledStatusCodesUseCase

class LibraryVisibilitySettingsDependencies(
    val getEnabledStatusCodesAsFlowUseCase: GetEnabledStatusCodesAsFlowUseCase,
    val getEnabledListIdsAsFlowUseCase: GetEnabledListIdsAsFlowUseCase,
    val setEnabledStatusCodesUseCase: SetEnabledStatusCodesUseCase,
    val setEnabledListIdsUseCase: SetEnabledListIdsUseCase,
    val getAllUserListsUseCase: GetAllUserListsUseCase,
    val refreshUserBooksUseCase: RefreshUserBooksUseCase,
    val applicationScope: ApplicationScope,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()

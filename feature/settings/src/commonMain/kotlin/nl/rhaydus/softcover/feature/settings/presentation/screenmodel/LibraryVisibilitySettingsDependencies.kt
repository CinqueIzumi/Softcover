package nl.rhaydus.softcover.feature.settings.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.book.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetDidNotFinishUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetReadUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetWantToReadUserBooksUseCase
import nl.rhaydus.softcover.core.domain.account.RefreshLibraryUseCase
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
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
    val getAllUserBooksUseCase: GetAllUserBooksUseCase,
    val getCurrentlyReadingUserBooksUseCase: GetCurrentlyReadingUserBooksUseCase,
    val getWantToReadUserBooksUseCase: GetWantToReadUserBooksUseCase,
    val getReadUserBooksUseCase: GetReadUserBooksUseCase,
    val getDidNotFinishUserBooksUseCase: GetDidNotFinishUserBooksUseCase,
    val refreshLibraryUseCase: RefreshLibraryUseCase,
    val applicationScope: ApplicationScope,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()

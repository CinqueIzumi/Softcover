package nl.rhaydus.softcover.feature.library.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.presentation.toad.ActionDependencies
import nl.rhaydus.softcover.feature.books.domain.usecase.GetAllUserListsUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.GetSortedAllUserBooksUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.GetSortedBooksByStatusUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.RefreshUserBooksUseCase
import nl.rhaydus.softcover.feature.deadlines.domain.usecase.ObserveAllBookDeadlinesUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetDateStyleAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetEnabledListIdsAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetEnabledStatusCodesAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetLibraryGridLayoutAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetLibrarySortSettingsAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetLibraryTabOrderAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetLibraryGridLayoutUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetLibrarySortUseCase

class LibraryDependencies(
    val getSortedAllUserBooksUseCase: GetSortedAllUserBooksUseCase,
    val getSortedBooksByStatusUseCase: GetSortedBooksByStatusUseCase,
    val refreshUserBooksUseCase: RefreshUserBooksUseCase,
    val getAllUserListsUseCase: GetAllUserListsUseCase,
    val getLibraryGridLayoutAsFlowUseCase: GetLibraryGridLayoutAsFlowUseCase,
    val setLibraryGridLayoutUseCase: SetLibraryGridLayoutUseCase,
    val getLibrarySortSettingsAsFlowUseCase: GetLibrarySortSettingsAsFlowUseCase,
    val setLibrarySortUseCase: SetLibrarySortUseCase,
    val observeAllBookDeadlinesUseCase: ObserveAllBookDeadlinesUseCase,
    val getDateStyleAsFlowUseCase: GetDateStyleAsFlowUseCase,
    val getEnabledStatusCodesAsFlowUseCase: GetEnabledStatusCodesAsFlowUseCase,
    val getEnabledListIdsAsFlowUseCase: GetEnabledListIdsAsFlowUseCase,
    val getLibraryTabOrderAsFlowUseCase: GetLibraryTabOrderAsFlowUseCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()

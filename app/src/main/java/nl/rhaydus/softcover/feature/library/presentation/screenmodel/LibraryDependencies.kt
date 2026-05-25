package nl.rhaydus.softcover.feature.library.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.presentation.toad.ActionDependencies
import nl.rhaydus.softcover.feature.books.domain.usecase.GetSortedAllUserBooksUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.GetSortedBooksByStatusUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.MarkBookAsReadingUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.MarkBookAsWantToReadUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.RemoveBookFromLibraryUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.ReorderShelfBooksUseCase
import nl.rhaydus.softcover.feature.deadlines.domain.usecase.ObserveAllBookDeadlinesUseCase
import nl.rhaydus.softcover.feature.library.domain.usecase.RefreshLibraryUseCase
import nl.rhaydus.softcover.feature.lists.domain.usecase.AddBookToListUseCase
import nl.rhaydus.softcover.feature.lists.domain.usecase.GetAllUserListsUseCase
import nl.rhaydus.softcover.feature.lists.domain.usecase.RemoveBookFromListUseCase
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
    val refreshLibraryUseCase: RefreshLibraryUseCase,
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
    val markBookAsReadUseCase: MarkBookAsReadUseCase,
    val markBookAsReadingUseCase: MarkBookAsReadingUseCase,
    val markBookAsWantToReadUseCase: MarkBookAsWantToReadUseCase,
    val removeBookFromLibraryUseCase: RemoveBookFromLibraryUseCase,
    val reorderShelfBooksUseCase: ReorderShelfBooksUseCase,
    val addBookToListUseCase: AddBookToListUseCase,
    val removeBookFromListUseCase: RemoveBookFromListUseCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
    val defaultDispatcher: CoroutineDispatcher,
) : ActionDependencies()

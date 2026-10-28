package nl.rhaydus.softcover.feature.library.di

import nl.rhaydus.softcover.core.book.di.bookModule
import nl.rhaydus.softcover.core.deadlines.di.deadlinesModule
import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import nl.rhaydus.softcover.core.lists.di.listsModule
import nl.rhaydus.softcover.core.preferences.di.preferencesModule
import nl.rhaydus.softcover.core.presentation.di.presentationModule
import nl.rhaydus.softcover.feature.library.presentation.collector.ActiveFilterChipsCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.ActiveSessionBookCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.AllBooksCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.ArrangeDraftChipsCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.ArrangeSheetCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.BookCardModelsCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.BookDeadlinesCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.BookListsCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.BooksByStatusCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.BulkRemoveSheetCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.ChooseListsCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.DateStyleCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.DisplayListsCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.EmptyStatesCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.EndSessionPauseSheetCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.FilterChipModelsCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.FilterDraftChipsCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.FilterOptionsCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.GridLayoutCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.LibraryCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.MastheadCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.PillButtonsCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.RearrangeChipCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.RecommendedBooksCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.RecommendedMenuCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.RecommendedRailCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.ShelfSidebarCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.ShelfSwipeCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.ShelvesSheetCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.SortModeCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.UserTagsCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.VisibleTabsCollector
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryScreenScreenModel
import org.koin.dsl.bind
import org.koin.dsl.module

val libraryModule = module {
    includes(
        dispatcherModule,
        bookModule,
        listsModule,
        deadlinesModule,
        preferencesModule,
        presentationModule,
    )

    factory { AllBooksCollector() } bind LibraryCollector::class

    factory { BooksByStatusCollector() } bind LibraryCollector::class

    factory { BookListsCollector() } bind LibraryCollector::class
    factory { BookCardModelsCollector() } bind LibraryCollector::class
    factory { ChooseListsCollector() } bind LibraryCollector::class

    factory { VisibleTabsCollector() } bind LibraryCollector::class
    factory { ShelfSidebarCollector() } bind LibraryCollector::class
    factory { EmptyStatesCollector() } bind LibraryCollector::class

    factory { GridLayoutCollector() } bind LibraryCollector::class
    factory { MastheadCollector() } bind LibraryCollector::class

    factory { ShelfSwipeCollector() } bind LibraryCollector::class

    factory { SortModeCollector() } bind LibraryCollector::class

    factory { BookDeadlinesCollector() } bind LibraryCollector::class
    factory { UserTagsCollector() } bind LibraryCollector::class

    factory { DateStyleCollector() } bind LibraryCollector::class

    factory { FilterOptionsCollector() } bind LibraryCollector::class
    factory { FilterChipModelsCollector() } bind LibraryCollector::class
    factory { ActiveFilterChipsCollector() } bind LibraryCollector::class
    factory { RearrangeChipCollector() } bind LibraryCollector::class
    factory { PillButtonsCollector() } bind LibraryCollector::class
    factory { ArrangeDraftChipsCollector() } bind LibraryCollector::class
    factory { FilterDraftChipsCollector() } bind LibraryCollector::class
    factory { ArrangeSheetCollector() } bind LibraryCollector::class
    factory { ShelvesSheetCollector() } bind LibraryCollector::class
    factory { BulkRemoveSheetCollector() } bind LibraryCollector::class
    factory { ActiveSessionBookCollector() } bind LibraryCollector::class
    factory { EndSessionPauseSheetCollector() } bind LibraryCollector::class

    factory { DisplayListsCollector() } bind LibraryCollector::class

    factory { RecommendedBooksCollector() } bind LibraryCollector::class
    factory { RecommendedRailCollector() } bind LibraryCollector::class
    factory { RecommendedMenuCollector() } bind LibraryCollector::class

    factory {
        LibraryScreenScreenModel(
            getSortedAllUserBooksUseCase = get(),
            getSortedBooksByStatusUseCase = get(),
            refreshLibraryUseCase = get(),
            appDispatchers = get(),
            flows = getAll(),
            getAllUserListsUseCase = get(),
            getLibraryGridLayoutAsFlowUseCase = get(),
            setLibraryGridLayoutUseCase = get(),
            getLibrarySortSettingsAsFlowUseCase = get(),
            setLibrarySortUseCase = get(),
            observeUserTagsByBookUseCase = get(),
            observeAllBookDeadlinesUseCase = get(),
            getDateStyleAsFlowUseCase = get(),
            getEnabledStatusCodesAsFlowUseCase = get(),
            getEnabledListIdsAsFlowUseCase = get(),
            getLibraryTabOrderAsFlowUseCase = get(),
            getShelfSwipeEnabledAsFlowUseCase = get(),
            markBookAsReadUseCase = get(),
            markBookAsReadingUseCase = get(),
            markBookAsWantToReadUseCase = get(),
            pauseBookUseCase = get(),
            activeSessionController = get(),
            undoableBookRemover = get(),
            reorderShelfBooksUseCase = get(),
            addBookToListUseCase = get(),
            removeBookFromListUseCase = get(),
            reorderListBooksUseCase = get(),
            setListRankedUseCase = get(),
            observeRecommendedFeedUseCase = get(),
            refreshRecommendedFeedUseCase = get(),
            hideRecommendedBookUseCase = get(),
            restoreRecommendedBookUseCase = get(),
        )
    }
}

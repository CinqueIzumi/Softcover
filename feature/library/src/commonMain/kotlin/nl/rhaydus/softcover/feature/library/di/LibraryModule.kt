package nl.rhaydus.softcover.feature.library.di

import nl.rhaydus.softcover.core.book.di.bookModule
import nl.rhaydus.softcover.core.deadlines.di.deadlinesModule
import nl.rhaydus.softcover.core.designsystem.presentation.di.designSystemModule
import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import nl.rhaydus.softcover.core.lists.di.listsModule
import nl.rhaydus.softcover.core.preferences.di.preferencesModule
import nl.rhaydus.softcover.feature.library.presentation.collector.AllBooksCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.BookDeadlinesCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.BookListsCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.BooksByStatusCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.DateStyleCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.FilterOptionsCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.GridLayoutCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.LibraryCollector
import nl.rhaydus.softcover.feature.library.presentation.collector.SortModeCollector
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
        designSystemModule,
    )

    factory { AllBooksCollector() } bind LibraryCollector::class

    factory { BooksByStatusCollector() } bind LibraryCollector::class

    factory { BookListsCollector() } bind LibraryCollector::class

    factory { VisibleTabsCollector() } bind LibraryCollector::class

    factory { GridLayoutCollector() } bind LibraryCollector::class

    factory { SortModeCollector() } bind LibraryCollector::class

    factory { BookDeadlinesCollector() } bind LibraryCollector::class

    factory { DateStyleCollector() } bind LibraryCollector::class

    factory { FilterOptionsCollector() } bind LibraryCollector::class

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
            observeAllBookDeadlinesUseCase = get(),
            getDateStyleAsFlowUseCase = get(),
            getEnabledStatusCodesAsFlowUseCase = get(),
            getEnabledListIdsAsFlowUseCase = get(),
            getLibraryTabOrderAsFlowUseCase = get(),
            markBookAsReadUseCase = get(),
            markBookAsReadingUseCase = get(),
            markBookAsWantToReadUseCase = get(),
            removeBookFromLibraryUseCase = get(),
            reorderShelfBooksUseCase = get(),
            addBookToListUseCase = get(),
            removeBookFromListUseCase = get(),
            reorderListBooksUseCase = get(),
            setListRankedUseCase = get(),
        )
    }
}

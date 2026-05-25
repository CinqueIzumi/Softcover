package nl.rhaydus.softcover.feature.library.di

import nl.rhaydus.softcover.feature.library.domain.usecase.RefreshLibraryUseCase
import nl.rhaydus.softcover.feature.library.presentation.flows.AllBooksCollector
import nl.rhaydus.softcover.feature.library.presentation.flows.BookDeadlinesCollector
import nl.rhaydus.softcover.feature.library.presentation.flows.BookListsCollector
import nl.rhaydus.softcover.feature.library.presentation.flows.BooksByStatusCollector
import nl.rhaydus.softcover.feature.library.presentation.flows.DateStyleCollector
import nl.rhaydus.softcover.feature.library.presentation.flows.FilterOptionsCollector
import nl.rhaydus.softcover.feature.library.presentation.flows.GridLayoutCollector
import nl.rhaydus.softcover.feature.library.presentation.flows.LibraryInitializer
import nl.rhaydus.softcover.feature.library.presentation.flows.SortModeCollector
import nl.rhaydus.softcover.feature.library.presentation.flows.VisibleTabsCollector
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryScreenScreenModel
import org.koin.dsl.bind
import org.koin.dsl.module

val libraryModule = module {
    factory { AllBooksCollector() } bind LibraryInitializer::class

    factory { BooksByStatusCollector() } bind LibraryInitializer::class

    factory { BookListsCollector() } bind LibraryInitializer::class

    factory { VisibleTabsCollector() } bind LibraryInitializer::class

    factory { GridLayoutCollector() } bind LibraryInitializer::class

    factory { SortModeCollector() } bind LibraryInitializer::class

    factory { BookDeadlinesCollector() } bind LibraryInitializer::class

    factory { DateStyleCollector() } bind LibraryInitializer::class

    factory { FilterOptionsCollector() } bind LibraryInitializer::class

    factory {
        RefreshLibraryUseCase(
            getUserIdUseCase = get(),
            booksRepository = get(),
            listsRepository = get(),
            settingsRepository = get(),
            dispatchers = get(),
        )
    }

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
        )
    }
}

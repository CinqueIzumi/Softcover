package nl.rhaydus.softcover.di

import nl.rhaydus.softcover.core.presentation.screen.MainActivityViewModel
import nl.rhaydus.softcover.feature.book.presentation.viewmodel.BookDetailScreenViewModel
import nl.rhaydus.softcover.feature.library.presentation.viewmodel.LibraryScreenViewModel
import nl.rhaydus.softcover.feature.onboarding.presentation.initializer.OnboardingInitializer
import nl.rhaydus.softcover.feature.onboarding.presentation.viewmodel.OnboardingScreenModel
import nl.rhaydus.softcover.feature.reading.presentation.viewmodel.ReadingScreenViewModel
import nl.rhaydus.softcover.feature.search.presentation.flows.SearchInitializer
import nl.rhaydus.softcover.feature.search.presentation.viewmodel.SearchScreenViewModel
import nl.rhaydus.softcover.feature.settings.presentation.viewmodel.SettingsScreenViewModel
import org.koin.dsl.module

val screenModelModule = module {
    factory {
        SettingsScreenViewModel(
            updateApiKeyUseCase = get(),
            getApiKeyUseCase = get(),
            resetUserDataUseCase = get(),
            appDispatchers = get(),
            flows = getAll(),
            initializeUserBooksUseCase = get(),
        )
    }

    factory {
        LibraryScreenViewModel(
            appDispatchers = get(),
            flows = getAll(),
            getAllUserBooksUseCase = get(),
            getWantToReadUserBooksUseCase = get(),
            getCurrentlyReadingUserBooksUseCase = get(),
            getReadUserBooksUseCase = get(),
            getDidNotFinishUserBooksUseCase = get(),
            initializeUserBooksUseCase = get(),
        )
    }

    factory {
        BookDetailScreenViewModel(
            fetchBookByIdUseCase = get(),
            updateBookEditionUseCase = get(),
            updateBookProgress = get(),
            getAllUserBooksUseCase = get(),
            appDispatchers = get(),
            markBookAsWantToReadUseCase = get(),
            markBookAsReadingUseCase = get(),
            removeBookFromLibraryUseCase = get(),
            flows = getAll(),
        )
    }

    factory {
        ReadingScreenViewModel(
            updateBookProgressUseCase = get(),
            markBookAsReadUseCase = get(),
            updateBookEditionUseCase = get(),
            refreshUserBooksUseCase = get(),
            updateBookProgress = get(),
            appDispatchers = get(),
            initializeUserBooksUseCase = get(),
            getCurrentlyReadingBooksUseCase = get(),
            flows = getAll(),
        )
    }

    factory {
        SearchScreenViewModel(
            flows = getAll<SearchInitializer>(),
            appDispatchers = get(),
            getPreviousSearchQueriesUseCase = get(),
            getQueriedBooksUseCase = get(),
            searchForNameUseCase = get(),
            removeSearchQueryUseCase = get(),
            removeAllSearchQueriesUseCase = get(),
            getAllUserBooksUseCase = get(),
            markBookAsWantToReadUseCase = get(),
            removeBookFromLibraryUseCase = get(),
        )
    }

    factory {
        OnboardingScreenModel(
            dispatchers = get(),
            initializers = getAll<OnboardingInitializer>(),
            initializeUserBooksUseCase = get(),
            initializeUserDataUseCase = get(),
            resetUserDataUseCase = get(),
            updateApiKeyUseCase = get(),
        )
    }

    factory {
        MainActivityViewModel(
            getUserIdUseCaseAsFlow = get(),
            initializeUserBooksUseCase = get()
        )
    }
}
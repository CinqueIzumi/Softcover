package nl.rhaydus.softcover.di

import nl.rhaydus.softcover.feature.book.presentation.viewmodel.BookDetailScreenViewModel
import nl.rhaydus.softcover.feature.library.presentation.viewmodel.LibraryScreenViewModel
import nl.rhaydus.softcover.feature.reading.presentation.viewmodel.ReadingScreenViewModel
import nl.rhaydus.softcover.feature.settings.presentation.viewmodel.SettingsScreenViewModel
import org.koin.dsl.module

val screenModelModule = module {
    factory {
        SettingsScreenViewModel(
            updateApiKeyUseCase = get(),
            getApiKeyUseCase = get(),
            initializeUserIdUseCase = get(),
            resetUserDataUseCase = get(),
            appDispatchers = get()
        )
    }

    factory {
        LibraryScreenViewModel(
            getUserBooksAsFlowUseCase = get(),
            appDispatchers = get(),
        )
    }

    factory {
        BookDetailScreenViewModel(
            fetchBookByIdUseCase = get(),
            updateBookEditionUseCase = get(),
            updateBookProgress = get(),
            getUserBooksAsFlowUseCase = get(),
            updateBookStatusUseCase = get(),
            appDispatchers = get()
        )
    }

    factory {
        ReadingScreenViewModel(
            getCurrentlyReadingBooksUseCase = get(),
            updateBookProgressUseCase = get(),
            markBookAsReadUseCase = get(),
            updateBookEditionUseCase = get(),
            refreshUserBooksUseCase = get(),
            updateBookProgress = get(),
            appDispatchers = get()
        )
    }
}
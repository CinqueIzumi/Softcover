package nl.rhaydus.softcover.core.library.di

import nl.rhaydus.softcover.core.library.domain.usecase.RefreshLibraryUseCase
import org.koin.dsl.module

val libraryServiceModule = module {
    factory {
        RefreshLibraryUseCase(
            getUserIdUseCase = get(),
            booksRepository = get(),
            listsRepository = get(),
            settingsRepository = get(),
            dispatchers = get(),
        )
    }
}

package nl.rhaydus.softcover.core.library.di

import nl.rhaydus.softcover.core.book.di.bookModule
import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import nl.rhaydus.softcover.core.identity.di.identityModule
import nl.rhaydus.softcover.core.library.domain.usecase.RefreshLibraryUseCase
import nl.rhaydus.softcover.core.lists.di.listsModule
import nl.rhaydus.softcover.core.preferences.di.preferencesModule
import org.koin.dsl.module

val libraryServiceModule = module {
    includes(
        dispatcherModule,
        bookModule,
        listsModule,
        preferencesModule,
        identityModule,
    )

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

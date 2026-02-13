package nl.rhaydus.softcover.feature.updated_library.di

import nl.rhaydus.softcover.feature.updated_library.presentation.flows.AllBooksCollector
import nl.rhaydus.softcover.feature.updated_library.presentation.flows.CurrentlyReadingBooksCollector
import nl.rhaydus.softcover.feature.updated_library.presentation.flows.DidNotFinishBooksCollector
import nl.rhaydus.softcover.feature.updated_library.presentation.flows.LibraryInitializer
import nl.rhaydus.softcover.feature.updated_library.presentation.flows.ReadBooksCollector
import nl.rhaydus.softcover.feature.updated_library.presentation.flows.WantToReadBooksCollector
import nl.rhaydus.softcover.feature.updated_library.presentation.screenmodel.LibraryScreenScreenModel
import org.koin.dsl.bind
import org.koin.dsl.module

val libraryModule = module {
    factory { AllBooksCollector() } bind LibraryInitializer::class

    factory { CurrentlyReadingBooksCollector() } bind LibraryInitializer::class

    factory { DidNotFinishBooksCollector() } bind LibraryInitializer::class

    factory { ReadBooksCollector() } bind LibraryInitializer::class

    factory { WantToReadBooksCollector() } bind LibraryInitializer::class

    factory {
        LibraryScreenScreenModel(
            getWantToReadUserBooksUseCase = get(),
            getCurrentlyReadingUserBooksUseCase = get(),
            getReadUserBooksUseCase = get(),
            getDidNotFinishUserBooksUseCase = get(),
            getAllUserBooksUseCase = get(),
            refreshUserBooksUseCase = get(),
            appDispatchers = get(),
            flows = getAll(),
        )
    }
}
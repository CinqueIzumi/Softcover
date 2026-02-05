package nl.rhaydus.softcover.feature.library.di

import nl.rhaydus.softcover.feature.library.presentation.flows.AllBooksCollector
import nl.rhaydus.softcover.feature.library.presentation.flows.CurrentlyReadingBooksCollector
import nl.rhaydus.softcover.feature.library.presentation.flows.DidNotFinishBooksCollector
import nl.rhaydus.softcover.feature.library.presentation.flows.LibraryInitializer
import nl.rhaydus.softcover.feature.library.presentation.flows.ReadBooksCollector
import nl.rhaydus.softcover.feature.library.presentation.flows.WantToReadBooksCollector
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryScreenScreenModel
import org.koin.dsl.bind
import org.koin.dsl.module

val libraryModule = module {
    factory { AllBooksCollector() } bind LibraryInitializer::class

    factory { CurrentlyReadingBooksCollector() } bind LibraryInitializer::class

    factory { DidNotFinishBooksCollector() } bind LibraryInitializer::class

    factory { ReadBooksCollector() } bind LibraryInitializer::class

    factory { WantToReadBooksCollector() } bind LibraryInitializer::class

    single {
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
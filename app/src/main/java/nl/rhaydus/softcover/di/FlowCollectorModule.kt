package nl.rhaydus.softcover.di

import nl.rhaydus.softcover.feature.book.presentation.flows.BookDetailInitializer
import nl.rhaydus.softcover.feature.book.presentation.flows.UserBooksFlowCollector
import nl.rhaydus.softcover.feature.library.presentation.flows.AllBooksCollector
import nl.rhaydus.softcover.feature.library.presentation.flows.DidNotFinishBooksCollector
import nl.rhaydus.softcover.feature.library.presentation.flows.LibraryInitializer
import nl.rhaydus.softcover.feature.library.presentation.flows.ReadBooksCollector
import nl.rhaydus.softcover.feature.library.presentation.flows.WantToReadBooksCollector
import nl.rhaydus.softcover.feature.reading.presentation.initializer.CurrentlyReadingBooksCollector
import nl.rhaydus.softcover.feature.reading.presentation.initializer.InitializeUserBooksInitializer
import nl.rhaydus.softcover.feature.reading.presentation.initializer.ReadingInitializer
import nl.rhaydus.softcover.feature.search.presentation.flows.PreviousQueriesCollector
import nl.rhaydus.softcover.feature.search.presentation.flows.QueriedBooksCollector
import nl.rhaydus.softcover.feature.search.presentation.flows.SearchInitializer
import org.koin.dsl.bind
import org.koin.dsl.module

val initializerModule = module {
    factory { AllBooksCollector() } bind LibraryInitializer::class
    factory { nl.rhaydus.softcover.feature.library.presentation.flows.CurrentlyReadingBooksCollector() } bind LibraryInitializer::class
    factory { DidNotFinishBooksCollector() } bind LibraryInitializer::class
    factory { ReadBooksCollector() } bind LibraryInitializer::class
    factory { WantToReadBooksCollector() } bind LibraryInitializer::class

    factory { UserBooksFlowCollector() } bind BookDetailInitializer::class

    factory { QueriedBooksCollector() } bind SearchInitializer::class
    factory { PreviousQueriesCollector() } bind SearchInitializer::class

    factory { CurrentlyReadingBooksCollector() } bind ReadingInitializer::class
    factory { InitializeUserBooksInitializer() } bind ReadingInitializer::class
}
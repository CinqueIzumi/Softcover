package nl.rhaydus.softcover.di

import nl.rhaydus.softcover.feature.book.presentation.flows.BookDetailFlowCollector
import nl.rhaydus.softcover.feature.book.presentation.flows.UserBooksFlowCollector
import nl.rhaydus.softcover.feature.library.presentation.flows.GetUserBooksFlowCollector
import nl.rhaydus.softcover.feature.library.presentation.flows.LibraryFlowCollector
import nl.rhaydus.softcover.feature.reading.presentation.flows.CurrentlyReadingBooksCollector
import nl.rhaydus.softcover.feature.reading.presentation.flows.ReadingFlowCollector
import nl.rhaydus.softcover.feature.search.presentation.flows.PreviousQueriesCollector
import nl.rhaydus.softcover.feature.search.presentation.flows.QueriedBooksCollector
import nl.rhaydus.softcover.feature.search.presentation.flows.SearchFlowCollector
import org.koin.dsl.bind
import org.koin.dsl.module

val flowCollectorModule = module {
    factory { GetUserBooksFlowCollector() } bind LibraryFlowCollector::class

    factory { UserBooksFlowCollector() } bind BookDetailFlowCollector::class

    factory { CurrentlyReadingBooksCollector() } bind ReadingFlowCollector::class

    factory { QueriedBooksCollector() } bind SearchFlowCollector::class

    factory { PreviousQueriesCollector() } bind SearchFlowCollector::class
}
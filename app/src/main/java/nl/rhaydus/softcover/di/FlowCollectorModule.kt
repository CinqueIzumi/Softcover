package nl.rhaydus.softcover.di

import nl.rhaydus.softcover.feature.book.presentation.flows.BookDetailFlowCollector
import nl.rhaydus.softcover.feature.book.presentation.flows.UserBooksFlowCollector
import nl.rhaydus.softcover.feature.library.presentation.flows.GetUserBooksFlowCollector
import nl.rhaydus.softcover.feature.library.presentation.flows.LibraryFlowCollector
import nl.rhaydus.softcover.feature.reading.presentation.flows.CurrentlyReadingBooksCollector
import nl.rhaydus.softcover.feature.reading.presentation.flows.ReadingFlowCollector
import org.koin.dsl.module

val flowCollectorModule = module {
    factory<LibraryFlowCollector> { GetUserBooksFlowCollector() }

    factory<BookDetailFlowCollector> { UserBooksFlowCollector() }

    factory<ReadingFlowCollector> { CurrentlyReadingBooksCollector() }
}
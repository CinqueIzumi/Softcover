package nl.rhaydus.softcover.feature.book_detail.di

import nl.rhaydus.softcover.feature.book_detail.presentation.flows.BookDetailInitializer
import nl.rhaydus.softcover.feature.book_detail.presentation.flows.UserBooksFlowCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailScreenScreenModel
import org.koin.dsl.bind
import org.koin.dsl.module

val bookDetailModule = module {
    factory { UserBooksFlowCollector() } bind BookDetailInitializer::class

    factory {
        BookDetailScreenScreenModel(
            fetchBookByIdUseCase = get(),
            updateBookEditionUseCase = get(),
            updateBookProgress = get(),
            getAllUserBooksUseCase = get(),
            markBookAsWantToReadUseCase = get(),
            markBookAsReadingUseCase = get(),
            removeBookFromLibraryUseCase = get(),
            markBookAsReadUseCase = get(),
            flows = getAll(),
            appDispatchers = get(),
        )
    }
}
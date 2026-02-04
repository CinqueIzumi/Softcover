package nl.rhaydus.softcover.feature.book.di

import nl.rhaydus.softcover.feature.book.data.datasource.BookDetailRemoteDataSource
import nl.rhaydus.softcover.feature.book.data.datasource.BookDetailRemoteDataSourceImpl
import nl.rhaydus.softcover.feature.book.data.repository.BookDetailRepositoryImpl
import nl.rhaydus.softcover.feature.book.domain.repository.BookDetailRepository
import nl.rhaydus.softcover.feature.book.domain.usecase.FetchBookByIdUseCase
import nl.rhaydus.softcover.feature.book.domain.usecase.MarkBookAsReadingUseCase
import nl.rhaydus.softcover.feature.book.domain.usecase.MarkBookAsWantToReadUseCase
import nl.rhaydus.softcover.feature.book.domain.usecase.RemoveBookFromLibraryUseCase
import nl.rhaydus.softcover.feature.book.presentation.flows.BookDetailInitializer
import nl.rhaydus.softcover.feature.book.presentation.flows.UserBooksFlowCollector
import nl.rhaydus.softcover.feature.book.presentation.screenmodel.BookDetailScreenScreenModel
import org.koin.dsl.bind
import org.koin.dsl.module

val bookModule = module {
    single<BookDetailRemoteDataSource> {
        BookDetailRemoteDataSourceImpl(apolloClient = get())
    }

    single<BookDetailRepository> {
        BookDetailRepositoryImpl(
            bookDetailRemoteDataSource = get()
        )
    }

    factory {
        FetchBookByIdUseCase(
            bookDetailRepository = get(),
            getAllUserBooksUseCase = get(),
        )
    }

    factory {
        MarkBookAsReadingUseCase(
            bookDetailRepository = get(),
            cachingRepository = get(),
        )
    }

    factory {
        MarkBookAsWantToReadUseCase(
            bookDetailRepository = get(),
            cachingRepository = get(),
        )
    }

    factory {
        RemoveBookFromLibraryUseCase(
            bookDetailRepository = get(),
            cachingRepository = get(),
        )
    }

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
            flows = getAll(),
            appDispatchers = get(),
        )
    }
}
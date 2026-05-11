package nl.rhaydus.softcover.feature.book_detail.di

import nl.rhaydus.softcover.feature.book_detail.data.datasource.BookReviewsRemoteDataSource
import nl.rhaydus.softcover.feature.book_detail.data.datasource.BookReviewsRemoteDataSourceImpl
import nl.rhaydus.softcover.feature.book_detail.data.repository.BookReviewsRepositoryImpl
import nl.rhaydus.softcover.feature.book_detail.domain.repository.BookReviewsRepository
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.GetTopBookReviewsUseCase
import nl.rhaydus.softcover.feature.book_detail.presentation.flows.BookDeadlineCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.flows.BookDetailInitializer
import nl.rhaydus.softcover.feature.book_detail.presentation.flows.DateStyleCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.flows.UserBooksFlowCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailScreenScreenModel
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookInitialCover
import org.koin.dsl.bind
import org.koin.dsl.module

val bookDetailModule = module {
    factory { UserBooksFlowCollector() } bind BookDetailInitializer::class
    factory { DateStyleCollector() } bind BookDetailInitializer::class
    factory { BookDeadlineCollector() } bind BookDetailInitializer::class

    single<BookReviewsRemoteDataSource> {
        BookReviewsRemoteDataSourceImpl(apolloClient = get())
    }

    single<BookReviewsRepository> {
        BookReviewsRepositoryImpl(bookReviewsRemoteDataSource = get())
    }

    factory {
        GetTopBookReviewsUseCase(bookReviewsRepository = get())
    }

    factory { params ->
        BookDetailScreenScreenModel(
            bookId = params.get(),
            initialCover = params.getOrNull<BookInitialCover>(),
            fetchBookByIdUseCase = get(),
            getEditionsByBookIdUseCase = get(),
            updateBookEditionUseCase = get(),
            updateBookProgress = get(),
            getAllUserBooksUseCase = get(),
            markBookAsWantToReadUseCase = get(),
            markBookAsReadingUseCase = get(),
            removeBookFromLibraryUseCase = get(),
            markBookAsReadUseCase = get(),
            flows = getAll(),
            appDispatchers = get(),
            getDateStyleAsFlowUseCase = get(),
            setEditionAsOwnedUseCase = get(),
            observeBookDeadlineUseCase = get(),
            setBookDeadlineUseCase = get(),
            clearBookDeadlineUseCase = get(),
            getTopBookReviewsUseCase = get(),
        )
    }
}
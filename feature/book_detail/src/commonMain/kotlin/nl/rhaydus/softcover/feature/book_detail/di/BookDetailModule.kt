package nl.rhaydus.softcover.feature.book_detail.di

import org.koin.dsl.bind
import org.koin.dsl.module
import nl.rhaydus.softcover.core.book.di.bookModule
import nl.rhaydus.softcover.core.database.di.databaseModule
import nl.rhaydus.softcover.core.deadlines.di.deadlinesModule
import nl.rhaydus.softcover.core.designsystem.presentation.di.designSystemModule
import nl.rhaydus.softcover.core.designsystem.presentation.model.BookInitialCover
import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import nl.rhaydus.softcover.core.identity.di.identityModule
import nl.rhaydus.softcover.core.lists.di.listsModule
import nl.rhaydus.softcover.core.network.di.apolloModule
import nl.rhaydus.softcover.core.preferences.di.preferencesModule
import nl.rhaydus.softcover.core.profile.di.profileModule
import nl.rhaydus.softcover.feature.book_detail.data.datasource.BookReviewsRemoteDataSource
import nl.rhaydus.softcover.feature.book_detail.data.datasource.BookReviewsRemoteDataSourceImpl
import nl.rhaydus.softcover.feature.book_detail.data.datasource.ReadingJournalHistoryRemoteDataSource
import nl.rhaydus.softcover.feature.book_detail.data.datasource.ReadingJournalHistoryRemoteDataSourceImpl
import nl.rhaydus.softcover.feature.book_detail.data.datasource.UserTagsRemoteDataSource
import nl.rhaydus.softcover.feature.book_detail.data.datasource.UserTagsRemoteDataSourceImpl
import nl.rhaydus.softcover.feature.book_detail.data.repository.BookReviewsRepositoryImpl
import nl.rhaydus.softcover.feature.book_detail.data.repository.ReadingJournalHistoryRepositoryImpl
import nl.rhaydus.softcover.feature.book_detail.data.repository.UserTagsRepositoryImpl
import nl.rhaydus.softcover.feature.book_detail.domain.repository.BookReviewsRepository
import nl.rhaydus.softcover.feature.book_detail.domain.repository.ReadingJournalHistoryRepository
import nl.rhaydus.softcover.feature.book_detail.domain.repository.UserTagsRepository
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.GetReadingJournalHistoryUseCase
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.GetTopBookReviewsUseCase
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.GetUserTagsUseCase
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.SaveUserTagsUseCase
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.BookDeadlineCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.BookDetailCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.CurrentUserCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.DateStyleCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.LastUsedProgressUnitCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.ReadingPaceForecastCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.UserBooksFlowCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.UserListsFlowCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.UserTagsCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailScreenScreenModel

val bookDetailModule = module {
    includes(
        bookModule,
        listsModule,
        deadlinesModule,
        profileModule,
        identityModule,
        preferencesModule,
        databaseModule,
        apolloModule,
        designSystemModule,
        dispatcherModule,
    )

    factory { UserBooksFlowCollector() } bind BookDetailCollector::class
    factory { DateStyleCollector() } bind BookDetailCollector::class
    factory { BookDeadlineCollector() } bind BookDetailCollector::class
    factory { UserListsFlowCollector() } bind BookDetailCollector::class
    factory { CurrentUserCollector() } bind BookDetailCollector::class
    factory { UserTagsCollector() } bind BookDetailCollector::class
    factory { LastUsedProgressUnitCollector() } bind BookDetailCollector::class
    factory { ReadingPaceForecastCollector() } bind BookDetailCollector::class

    single<BookReviewsRemoteDataSource> {
        BookReviewsRemoteDataSourceImpl(apolloClient = get())
    }

    single<BookReviewsRepository> {
        BookReviewsRepositoryImpl(bookReviewsRemoteDataSource = get())
    }

    factory {
        GetTopBookReviewsUseCase(bookReviewsRepository = get())
    }

    single<UserTagsRemoteDataSource> {
        UserTagsRemoteDataSourceImpl(apolloClient = get())
    }

    single<UserTagsRepository> {
        UserTagsRepositoryImpl(userTagsRemoteDataSource = get())
    }

    factory {
        GetUserTagsUseCase(
            userTagsRepository = get(),
            getUserIdUseCase = get(),
        )
    }

    factory {
        SaveUserTagsUseCase(userTagsRepository = get())
    }

    single<ReadingJournalHistoryRemoteDataSource> {
        ReadingJournalHistoryRemoteDataSourceImpl(
            apolloClient = get(),
            getUserIdUseCase = get(),
        )
    }

    single<ReadingJournalHistoryRepository> {
        ReadingJournalHistoryRepositoryImpl(readingJournalHistoryRemoteDataSource = get())
    }

    factory {
        GetReadingJournalHistoryUseCase(readingJournalHistoryRepository = get())
    }

    factory { params ->
        BookDetailScreenScreenModel(
            bookId = params.get(),
            initialCover = params.getOrNull<BookInitialCover>(),
            fetchBookByIdUseCase = get(),
            getEditionsByBookIdUseCase = get(),
            updateBookEditionUseCase = get(),
            recordBookProgressUseCase = get(),
            getAllUserBooksUseCase = get(),
            markBookAsWantToReadUseCase = get(),
            markBookAsReadingUseCase = get(),
            removeBookFromLibraryUseCase = get(),
            markBookAsReadUseCase = get(),
            updateBookRatingUseCase = get(),
            flows = getAll(),
            appDispatchers = get(),
            getDateStyleAsFlowUseCase = get(),
            setEditionAsOwnedUseCase = get(),
            getAllUserListsUseCase = get(),
            addBookToListUseCase = get(),
            removeBookFromListUseCase = get(),
            observeBookDeadlineUseCase = get(),
            setBookDeadlineUseCase = get(),
            clearBookDeadlineUseCase = get(),
            getTopBookReviewsUseCase = get(),
            getReadingJournalHistoryUseCase = get(),
            updateBookReviewUseCase = get(),
            observeUserProfileDataUseCase = get(),
            getUserTagsUseCase = get(),
            saveUserTagsUseCase = get(),
            getLastUsedProgressUnitAsFlowUseCase = get(),
            setLastUsedProgressUnitUseCase = get(),
        )
    }
}

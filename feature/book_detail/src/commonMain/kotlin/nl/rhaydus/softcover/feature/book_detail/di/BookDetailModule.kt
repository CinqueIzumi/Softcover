package nl.rhaydus.softcover.feature.book_detail.di

import nl.rhaydus.softcover.core.book.di.bookModule
import nl.rhaydus.softcover.core.corpus.di.corpusModule
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import nl.rhaydus.softcover.core.database.di.databaseModule
import nl.rhaydus.softcover.core.deadlines.di.deadlinesModule
import nl.rhaydus.softcover.core.domain.di.currentDateModule
import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import nl.rhaydus.softcover.core.identity.di.identityModule
import nl.rhaydus.softcover.core.lists.di.listsModule
import nl.rhaydus.softcover.core.network.di.apolloModule
import nl.rhaydus.softcover.core.personal.di.personalModule
import nl.rhaydus.softcover.core.preferences.di.preferencesModule
import nl.rhaydus.softcover.core.presentation.di.presentationModule
import nl.rhaydus.softcover.core.presentation.model.BookInitialCover
import nl.rhaydus.softcover.core.profile.di.profileModule
import nl.rhaydus.softcover.core.progresssheet.di.progressSheetModule
import nl.rhaydus.softcover.feature.book_detail.data.datasource.BookReviewsRemoteDataSource
import nl.rhaydus.softcover.feature.book_detail.data.datasource.BookReviewsRemoteDataSourceImpl
import nl.rhaydus.softcover.feature.book_detail.data.datasource.UserTagVocabularyLocalDataSource
import nl.rhaydus.softcover.feature.book_detail.data.datasource.UserTagVocabularyLocalDataSourceImpl
import nl.rhaydus.softcover.feature.book_detail.data.datasource.UserTagVocabularyRemoteDataSource
import nl.rhaydus.softcover.feature.book_detail.data.datasource.UserTagVocabularyRemoteDataSourceImpl
import nl.rhaydus.softcover.feature.book_detail.data.repository.BookReviewsRepositoryImpl
import nl.rhaydus.softcover.feature.book_detail.data.repository.UserTagVocabularyRepositoryImpl
import nl.rhaydus.softcover.feature.book_detail.domain.repository.BookReviewsRepository
import nl.rhaydus.softcover.feature.book_detail.domain.repository.UserTagVocabularyRepository
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.GetTopBookReviewsUseCase
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.ObserveUserTagVocabularyUseCase
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.RecordAppliedTagsUseCase
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.SyncUserTagVocabularyUseCase
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.ActiveSessionBookCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.BookDeadlineCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.BookDetailCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.CalloutsCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.ChooseListsCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.CoverModelsCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.CurrentUserCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.DateStyleCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.EditionCardsCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.EditionSelectionCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.EditionSheetCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.EndSessionPauseSheetCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.ExternalLinksCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.HowItReadSectionCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.HowItReadSheetCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.InProgressCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.LensToggleCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.OfflineStateCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.ProgressSheetCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.QuoteDeleteSheetCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.QuoteSheetCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.QuotesRefreshCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.QuotesSectionCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.ReadingPaceForecastCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.ReviewsSummaryCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.ShareCardsCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.ShelveControlHeaderCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.ShelveReadingStatusCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.SimilarBooksLoadCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.SimilarBooksRailCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.TagChipModelsCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.TagEditorChipModelsCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.TagEditorSheetCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.TagNamingFieldCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.TagSuggestionsCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.TagVocabularySyncCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.UnreleasedBadgeCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.UserBooksFlowCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.UserListsFlowCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.UserTagsCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.VerdictReviewCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.VerdictSheetCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.YourReadsCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailScreenScreenModel
import org.koin.dsl.bind
import org.koin.dsl.module

val bookDetailModule = module {
    includes(
        bookModule,
        listsModule,
        deadlinesModule,
        corpusModule,
        profileModule,
        identityModule,
        preferencesModule,
        databaseModule,
        apolloModule,
        personalModule,
        progressSheetModule,
        presentationModule,
        currentDateModule,
        dispatcherModule,
    )

    factory { UserBooksFlowCollector() } bind BookDetailCollector::class
    factory { CoverModelsCollector() } bind BookDetailCollector::class
    factory { DateStyleCollector() } bind BookDetailCollector::class
    factory { OfflineStateCollector() } bind BookDetailCollector::class
    factory { BookDeadlineCollector() } bind BookDetailCollector::class
    factory { ReviewsSummaryCollector() } bind BookDetailCollector::class
    factory { SimilarBooksLoadCollector() } bind BookDetailCollector::class
    factory { SimilarBooksRailCollector() } bind BookDetailCollector::class
    factory { UnreleasedBadgeCollector() } bind BookDetailCollector::class
    factory { ExternalLinksCollector() } bind BookDetailCollector::class
    factory { CalloutsCollector() } bind BookDetailCollector::class
    factory { EditionSelectionCollector() } bind BookDetailCollector::class
    factory { EditionCardsCollector() } bind BookDetailCollector::class
    factory { UserListsFlowCollector() } bind BookDetailCollector::class
    factory { CurrentUserCollector() } bind BookDetailCollector::class
    factory { UserTagsCollector() } bind BookDetailCollector::class
    factory { ReadingPaceForecastCollector() } bind BookDetailCollector::class
    factory { TagSuggestionsCollector() } bind BookDetailCollector::class
    factory { TagChipModelsCollector() } bind BookDetailCollector::class
    factory { TagEditorChipModelsCollector() } bind BookDetailCollector::class
    factory { TagNamingFieldCollector() } bind BookDetailCollector::class
    factory { TagVocabularySyncCollector() } bind BookDetailCollector::class
    factory { VerdictReviewCollector() } bind BookDetailCollector::class
    factory { VerdictSheetCollector() } bind BookDetailCollector::class
    factory { InProgressCollector() } bind BookDetailCollector::class
    factory { ShelveReadingStatusCollector() } bind BookDetailCollector::class
    factory { YourReadsCollector() } bind BookDetailCollector::class
    factory { ShareCardsCollector() } bind BookDetailCollector::class
    factory { TagEditorSheetCollector() } bind BookDetailCollector::class
    factory { EditionSheetCollector() } bind BookDetailCollector::class
    factory { ProgressSheetCollector() } bind BookDetailCollector::class
    factory { ChooseListsCollector() } bind BookDetailCollector::class
    factory { ShelveControlHeaderCollector() } bind BookDetailCollector::class
    factory { ActiveSessionBookCollector() } bind BookDetailCollector::class
    factory { EndSessionPauseSheetCollector() } bind BookDetailCollector::class
    factory { LensToggleCollector() } bind BookDetailCollector::class
    factory { QuotesSectionCollector() } bind BookDetailCollector::class
    factory { QuotesRefreshCollector() } bind BookDetailCollector::class
    factory { QuoteSheetCollector() } bind BookDetailCollector::class
    factory { QuoteDeleteSheetCollector() } bind BookDetailCollector::class
    factory { HowItReadSectionCollector() } bind BookDetailCollector::class
    factory { HowItReadSheetCollector() } bind BookDetailCollector::class

    single<BookReviewsRemoteDataSource> {
        BookReviewsRemoteDataSourceImpl(apolloClient = get())
    }

    single<BookReviewsRepository> {
        BookReviewsRepositoryImpl(bookReviewsRemoteDataSource = get())
    }

    factory {
        GetTopBookReviewsUseCase(bookReviewsRepository = get())
    }

    single<UserTagVocabularyRemoteDataSource> {
        UserTagVocabularyRemoteDataSourceImpl(apolloClient = get())
    }

    single { get<SoftcoverDatabase>().userTagVocabularyDao() }

    single<UserTagVocabularyLocalDataSource> {
        UserTagVocabularyLocalDataSourceImpl(dao = get())
    }

    single<UserTagVocabularyRepository> {
        UserTagVocabularyRepositoryImpl(
            remoteDataSource = get(),
            localDataSource = get(),
        )
    }

    factory {
        SyncUserTagVocabularyUseCase(
            userTagVocabularyRepository = get(),
            getUserIdUseCase = get(),
        )
    }

    factory {
        ObserveUserTagVocabularyUseCase(
            userTagVocabularyRepository = get(),
            getUserIdUseCase = get(),
        )
    }

    factory {
        RecordAppliedTagsUseCase(
            userTagVocabularyRepository = get(),
            getUserIdUseCase = get(),
        )
    }

    factory { params ->
        BookDetailScreenScreenModel(
            bookId = params.get(),
            initialCover = params.getOrNull<BookInitialCover>(),
            transitionSurface = params.getOrNull<String>(),
            showVerdictSheet = params.getOrNull<Boolean>() ?: false,
            fetchBookByIdUseCase = get(),
            getEditionsByBookIdUseCase = get(),
            updateBookEditionUseCase = get(),
            recordBookProgressUseCase = get(),
            observeUserBookUseCase = get(),
            markBookAsWantToReadUseCase = get(),
            markBookAsReadingUseCase = get(),
            undoableBookRemover = get(),
            markBookAsReadUseCase = get(),
            pauseBookUseCase = get(),
            activeSessionController = get(),
            saveBookVerdictUseCase = get(),
            flows = getAll(),
            offlineCopy = get(),
            appDispatchers = get(),
            getDateStyleAsFlowUseCase = get(),
            observeCurrentDateUseCase = get(),
            setEditionAsOwnedUseCase = get(),
            getAllUserListsUseCase = get(),
            addBookToListUseCase = get(),
            removeBookFromListUseCase = get(),
            observeBookDeadlineUseCase = get(),
            setBookDeadlineUseCase = get(),
            clearBookDeadlineUseCase = get(),
            getTopBookReviewsUseCase = get(),
            observeReadingJournalHistoryUseCase = get(),
            refreshReadingJournalHistoryUseCase = get(),
            updateBookReviewUseCase = get(),
            observeUserProfileDataUseCase = get(),
            observeUserTagsForBookUseCase = get(),
            refreshUserTagsForBookUseCase = get(),
            saveUserTagsUseCase = get(),
            progressSheetSlice = get(),
            observeUserTagVocabularyUseCase = get(),
            syncUserTagVocabularyUseCase = get(),
            recordAppliedTagsUseCase = get(),
            addQuoteUseCase = get(),
            deleteQuoteUseCase = get(),
            observeBookQuotesUseCase = get(),
            refreshBookQuotesUseCase = get(),
            loadSimilarBooksPageUseCase = get(),
            observeShelvedBookIdsUseCase = get(),
            observeBookTraitsUseCase = get(),
            getBookTraitsUseCase = get(),
            saveBookTraitsUseCase = get(),
        )
    }
}

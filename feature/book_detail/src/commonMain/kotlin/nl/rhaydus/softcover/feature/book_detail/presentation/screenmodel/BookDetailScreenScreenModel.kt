package nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.usecase.FetchBookByIdUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetEditionsByBookIdUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.LoadSimilarBooksPageUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsReadingUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsWantToReadUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveShelvedBookIdsUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveUserBookUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.PauseBookUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RecordBookProgressUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.SaveBookVerdictUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.UpdateBookEditionUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.UpdateBookReviewUseCase
import nl.rhaydus.softcover.core.component.state.OfflineCopy
import nl.rhaydus.softcover.core.corpus.domain.usecase.GetBookTraitsUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.ObserveBookTraitsUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.SaveBookTraitsUseCase
import nl.rhaydus.softcover.core.deadlines.domain.usecase.ClearBookDeadlineUseCase
import nl.rhaydus.softcover.core.deadlines.domain.usecase.ObserveBookDeadlineUseCase
import nl.rhaydus.softcover.core.deadlines.domain.usecase.SetBookDeadlineUseCase
import nl.rhaydus.softcover.core.domain.util.ObserveCurrentDateUseCase
import nl.rhaydus.softcover.core.lists.domain.usecase.AddBookToListUseCase
import nl.rhaydus.softcover.core.lists.domain.usecase.GetAllUserListsUseCase
import nl.rhaydus.softcover.core.lists.domain.usecase.RemoveBookFromListUseCase
import nl.rhaydus.softcover.core.lists.domain.usecase.SetEditionAsOwnedUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.AddQuoteUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.DeleteQuoteUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.ObserveBookQuotesUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.ObserveReadingJournalHistoryUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.RefreshBookQuotesUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.RefreshReadingJournalHistoryUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetDateStyleAsFlowUseCase
import nl.rhaydus.softcover.core.presentation.model.BookInitialCover
import nl.rhaydus.softcover.core.presentation.removal.UndoableBookRemover
import nl.rhaydus.softcover.core.presentation.session.ActiveSessionController
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveUserProfileDataUseCase
import nl.rhaydus.softcover.core.progresssheet.presentation.ProgressSheetSlice
import nl.rhaydus.softcover.core.uibinding.verdict.VerdictSheetContext
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.GetTopBookReviewsUseCase
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.GetUserTagsUseCase
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.ObserveUserTagVocabularyUseCase
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.RecordAppliedTagsUseCase
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.SaveUserTagsUseCase
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.SyncUserTagVocabularyUseCase
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.FetchBookReviewsAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.InitializeBookWithIdAction
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.BookDetailCollector
import nl.rhaydus.softcover.feature.book_detail.presentation.collector.withCoverModels
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ToadScreenModel

internal class BookDetailScreenScreenModel(
    private val bookId: Int,
    initialCover: BookInitialCover?,
    transitionSurface: String?,
    showVerdictSheet: Boolean,
    private val fetchBookByIdUseCase: FetchBookByIdUseCase,
    private val getEditionsByBookIdUseCase: GetEditionsByBookIdUseCase,
    private val updateBookEditionUseCase: UpdateBookEditionUseCase,
    private val recordBookProgressUseCase: RecordBookProgressUseCase,
    private val observeUserBookUseCase: ObserveUserBookUseCase,
    private val markBookAsWantToReadUseCase: MarkBookAsWantToReadUseCase,
    private val markBookAsReadingUseCase: MarkBookAsReadingUseCase,
    private val undoableBookRemover: UndoableBookRemover,
    private val markBookAsReadUseCase: MarkBookAsReadUseCase,
    private val pauseBookUseCase: PauseBookUseCase,
    private val activeSessionController: ActiveSessionController,
    private val saveBookVerdictUseCase: SaveBookVerdictUseCase,
    private val getDateStyleAsFlowUseCase: GetDateStyleAsFlowUseCase,
    private val observeCurrentDateUseCase: ObserveCurrentDateUseCase,
    private val setEditionAsOwnedUseCase: SetEditionAsOwnedUseCase,
    private val getAllUserListsUseCase: GetAllUserListsUseCase,
    private val addBookToListUseCase: AddBookToListUseCase,
    private val removeBookFromListUseCase: RemoveBookFromListUseCase,
    private val observeBookDeadlineUseCase: ObserveBookDeadlineUseCase,
    private val setBookDeadlineUseCase: SetBookDeadlineUseCase,
    private val clearBookDeadlineUseCase: ClearBookDeadlineUseCase,
    private val getTopBookReviewsUseCase: GetTopBookReviewsUseCase,
    private val observeReadingJournalHistoryUseCase: ObserveReadingJournalHistoryUseCase,
    private val refreshReadingJournalHistoryUseCase: RefreshReadingJournalHistoryUseCase,
    private val updateBookReviewUseCase: UpdateBookReviewUseCase,
    private val observeUserProfileDataUseCase: ObserveUserProfileDataUseCase,
    private val getUserTagsUseCase: GetUserTagsUseCase,
    private val saveUserTagsUseCase: SaveUserTagsUseCase,
    private val progressSheetSlice: ProgressSheetSlice,
    private val observeUserTagVocabularyUseCase: ObserveUserTagVocabularyUseCase,
    private val syncUserTagVocabularyUseCase: SyncUserTagVocabularyUseCase,
    private val recordAppliedTagsUseCase: RecordAppliedTagsUseCase,
    private val addQuoteUseCase: AddQuoteUseCase,
    private val deleteQuoteUseCase: DeleteQuoteUseCase,
    private val observeBookQuotesUseCase: ObserveBookQuotesUseCase,
    private val refreshBookQuotesUseCase: RefreshBookQuotesUseCase,
    private val loadSimilarBooksPageUseCase: LoadSimilarBooksPageUseCase,
    private val observeShelvedBookIdsUseCase: ObserveShelvedBookIdsUseCase,
    private val observeBookTraitsUseCase: ObserveBookTraitsUseCase,
    private val getBookTraitsUseCase: GetBookTraitsUseCase,
    private val saveBookTraitsUseCase: SaveBookTraitsUseCase,
    offlineCopy: OfflineCopy,
    flows: List<BookDetailCollector>,
    appDispatchers: AppDispatchers,
) : ToadScreenModel<BookDetailUiState, BookDetailEvent, BookDetailDependencies, BookDetailCollector, BookDetailLocalVariables>(
    initialState = BookDetailUiState(
        bookId = bookId,
        transitionSurface = transitionSurface,
        initialCover = initialCover,
        scannedEditionId = initialCover?.scannedEditionId,
        verdictSheetContext = VerdictSheetContext.FINISHED.takeIf { showVerdictSheet },
    ).withCoverModels(),
    initialLocalVariables = BookDetailLocalVariables(),
    initializers = flows,
) {
    override val dependencies: BookDetailDependencies = BookDetailDependencies(
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
        fetchBookByIdUseCase = fetchBookByIdUseCase,
        getEditionsByBookIdUseCase = getEditionsByBookIdUseCase,
        updateBookEditionUseCase = updateBookEditionUseCase,
        recordBookProgressUseCase = recordBookProgressUseCase,
        observeUserBookUseCase = observeUserBookUseCase,
        markBookAsWantToReadUseCase = markBookAsWantToReadUseCase,
        markBookAsReadingUseCase = markBookAsReadingUseCase,
        undoableBookRemover = undoableBookRemover,
        markBookAsReadUseCase = markBookAsReadUseCase,
        pauseBookUseCase = pauseBookUseCase,
        activeSessionController = activeSessionController,
        saveBookVerdictUseCase = saveBookVerdictUseCase,
        getDateStyleAsFlowUseCase = getDateStyleAsFlowUseCase,
        observeCurrentDateUseCase = observeCurrentDateUseCase,
        setEditionAsOwnedUseCase = setEditionAsOwnedUseCase,
        getAllUserListsUseCase = getAllUserListsUseCase,
        addBookToListUseCase = addBookToListUseCase,
        removeBookFromListUseCase = removeBookFromListUseCase,
        observeBookDeadlineUseCase = observeBookDeadlineUseCase,
        setBookDeadlineUseCase = setBookDeadlineUseCase,
        clearBookDeadlineUseCase = clearBookDeadlineUseCase,
        getTopBookReviewsUseCase = getTopBookReviewsUseCase,
        observeReadingJournalHistoryUseCase = observeReadingJournalHistoryUseCase,
        refreshReadingJournalHistoryUseCase = refreshReadingJournalHistoryUseCase,
        updateBookReviewUseCase = updateBookReviewUseCase,
        observeUserProfileDataUseCase = observeUserProfileDataUseCase,
        getUserTagsUseCase = getUserTagsUseCase,
        saveUserTagsUseCase = saveUserTagsUseCase,
        progressSheetSlice = progressSheetSlice,
        observeUserTagVocabularyUseCase = observeUserTagVocabularyUseCase,
        syncUserTagVocabularyUseCase = syncUserTagVocabularyUseCase,
        recordAppliedTagsUseCase = recordAppliedTagsUseCase,
        addQuoteUseCase = addQuoteUseCase,
        deleteQuoteUseCase = deleteQuoteUseCase,
        observeBookQuotesUseCase = observeBookQuotesUseCase,
        refreshBookQuotesUseCase = refreshBookQuotesUseCase,
        loadSimilarBooksPageUseCase = loadSimilarBooksPageUseCase,
        observeShelvedBookIdsUseCase = observeShelvedBookIdsUseCase,
        observeBookTraitsUseCase = observeBookTraitsUseCase,
        getBookTraitsUseCase = getBookTraitsUseCase,
        saveBookTraitsUseCase = saveBookTraitsUseCase,
        offlineCopy = offlineCopy,
    )

    init {
        startInitializers()

        dispatch(InitializeBookWithIdAction(id = bookId))

        dispatch(FetchBookReviewsAction(bookId = bookId))
    }

    fun runAction(action: BookDetailAction) = dispatch(action)
}

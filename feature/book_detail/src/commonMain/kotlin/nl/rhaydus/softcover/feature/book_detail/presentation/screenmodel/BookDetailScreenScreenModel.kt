package nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.usecase.FetchBookByIdUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetEditionsByBookIdUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsReadingUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsWantToReadUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RecordBookProgressUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RemoveBookFromLibraryUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.SaveBookVerdictUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.UpdateBookEditionUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.UpdateBookReviewUseCase
import nl.rhaydus.softcover.core.deadlines.domain.usecase.ClearBookDeadlineUseCase
import nl.rhaydus.softcover.core.deadlines.domain.usecase.ObserveBookDeadlineUseCase
import nl.rhaydus.softcover.core.deadlines.domain.usecase.SetBookDeadlineUseCase
import nl.rhaydus.softcover.core.lists.domain.usecase.AddBookToListUseCase
import nl.rhaydus.softcover.core.lists.domain.usecase.GetAllUserListsUseCase
import nl.rhaydus.softcover.core.lists.domain.usecase.RemoveBookFromListUseCase
import nl.rhaydus.softcover.core.lists.domain.usecase.SetEditionAsOwnedUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.GetReadingJournalHistoryUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetDateStyleAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetLastUsedProgressUnitAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetLastUsedProgressUnitUseCase
import nl.rhaydus.softcover.core.presentation.model.BookInitialCover
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveUserProfileDataUseCase
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
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ToadScreenModel

internal class BookDetailScreenScreenModel(
    private val bookId: Int,
    initialCover: BookInitialCover?,
    private val fetchBookByIdUseCase: FetchBookByIdUseCase,
    private val getEditionsByBookIdUseCase: GetEditionsByBookIdUseCase,
    private val updateBookEditionUseCase: UpdateBookEditionUseCase,
    private val recordBookProgressUseCase: RecordBookProgressUseCase,
    private val getAllUserBooksUseCase: GetAllUserBooksUseCase,
    private val markBookAsWantToReadUseCase: MarkBookAsWantToReadUseCase,
    private val markBookAsReadingUseCase: MarkBookAsReadingUseCase,
    private val removeBookFromLibraryUseCase: RemoveBookFromLibraryUseCase,
    private val markBookAsReadUseCase: MarkBookAsReadUseCase,
    private val saveBookVerdictUseCase: SaveBookVerdictUseCase,
    private val getDateStyleAsFlowUseCase: GetDateStyleAsFlowUseCase,
    private val setEditionAsOwnedUseCase: SetEditionAsOwnedUseCase,
    private val getAllUserListsUseCase: GetAllUserListsUseCase,
    private val addBookToListUseCase: AddBookToListUseCase,
    private val removeBookFromListUseCase: RemoveBookFromListUseCase,
    private val observeBookDeadlineUseCase: ObserveBookDeadlineUseCase,
    private val setBookDeadlineUseCase: SetBookDeadlineUseCase,
    private val clearBookDeadlineUseCase: ClearBookDeadlineUseCase,
    private val getTopBookReviewsUseCase: GetTopBookReviewsUseCase,
    private val getReadingJournalHistoryUseCase: GetReadingJournalHistoryUseCase,
    private val updateBookReviewUseCase: UpdateBookReviewUseCase,
    private val observeUserProfileDataUseCase: ObserveUserProfileDataUseCase,
    private val getUserTagsUseCase: GetUserTagsUseCase,
    private val saveUserTagsUseCase: SaveUserTagsUseCase,
    private val getLastUsedProgressUnitAsFlowUseCase: GetLastUsedProgressUnitAsFlowUseCase,
    private val setLastUsedProgressUnitUseCase: SetLastUsedProgressUnitUseCase,
    private val observeUserTagVocabularyUseCase: ObserveUserTagVocabularyUseCase,
    private val syncUserTagVocabularyUseCase: SyncUserTagVocabularyUseCase,
    private val recordAppliedTagsUseCase: RecordAppliedTagsUseCase,
    flows: List<BookDetailCollector>,
    appDispatchers: AppDispatchers,
) : ToadScreenModel<BookDetailUiState, BookDetailEvent, BookDetailDependencies, BookDetailCollector, BookDetailLocalVariables>(
    initialState = BookDetailUiState(
        initialCover = initialCover,
        scannedEditionId = initialCover?.scannedEditionId,
    ),
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
        getAllUserBooksUseCase = getAllUserBooksUseCase,
        markBookAsWantToReadUseCase = markBookAsWantToReadUseCase,
        markBookAsReadingUseCase = markBookAsReadingUseCase,
        removeBookFromLibraryUseCase = removeBookFromLibraryUseCase,
        markBookAsReadUseCase = markBookAsReadUseCase,
        saveBookVerdictUseCase = saveBookVerdictUseCase,
        getDateStyleAsFlowUseCase = getDateStyleAsFlowUseCase,
        setEditionAsOwnedUseCase = setEditionAsOwnedUseCase,
        getAllUserListsUseCase = getAllUserListsUseCase,
        addBookToListUseCase = addBookToListUseCase,
        removeBookFromListUseCase = removeBookFromListUseCase,
        observeBookDeadlineUseCase = observeBookDeadlineUseCase,
        setBookDeadlineUseCase = setBookDeadlineUseCase,
        clearBookDeadlineUseCase = clearBookDeadlineUseCase,
        getTopBookReviewsUseCase = getTopBookReviewsUseCase,
        getReadingJournalHistoryUseCase = getReadingJournalHistoryUseCase,
        updateBookReviewUseCase = updateBookReviewUseCase,
        observeUserProfileDataUseCase = observeUserProfileDataUseCase,
        getUserTagsUseCase = getUserTagsUseCase,
        saveUserTagsUseCase = saveUserTagsUseCase,
        getLastUsedProgressUnitAsFlowUseCase = getLastUsedProgressUnitAsFlowUseCase,
        setLastUsedProgressUnitUseCase = setLastUsedProgressUnitUseCase,
        observeUserTagVocabularyUseCase = observeUserTagVocabularyUseCase,
        syncUserTagVocabularyUseCase = syncUserTagVocabularyUseCase,
        recordAppliedTagsUseCase = recordAppliedTagsUseCase,
    )

    init {
        startInitializers()

        dispatch(InitializeBookWithIdAction(id = bookId))

        dispatch(FetchBookReviewsAction(bookId = bookId))
    }

    fun runAction(action: BookDetailAction) = dispatch(action)
}

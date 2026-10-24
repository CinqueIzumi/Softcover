package nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
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
import nl.rhaydus.softcover.core.presentation.removal.UndoableBookRemover
import nl.rhaydus.softcover.core.presentation.session.ActiveSessionController
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveUserProfileDataUseCase
import nl.rhaydus.softcover.core.progresssheet.presentation.ProgressSheetSlice
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.GetTopBookReviewsUseCase
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.GetUserTagsUseCase
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.ObserveUserTagVocabularyUseCase
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.RecordAppliedTagsUseCase
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.SaveUserTagsUseCase
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.SyncUserTagVocabularyUseCase
import nl.rhaydus.toad.ActionDependencies

internal class BookDetailDependencies(
    val recordBookProgressUseCase: RecordBookProgressUseCase,
    val updateBookEditionUseCase: UpdateBookEditionUseCase,
    val fetchBookByIdUseCase: FetchBookByIdUseCase,
    val getEditionsByBookIdUseCase: GetEditionsByBookIdUseCase,
    val observeUserBookUseCase: ObserveUserBookUseCase,
    val markBookAsWantToReadUseCase: MarkBookAsWantToReadUseCase,
    val markBookAsReadingUseCase: MarkBookAsReadingUseCase,
    val markBookAsReadUseCase: MarkBookAsReadUseCase,
    val pauseBookUseCase: PauseBookUseCase,
    val activeSessionController: ActiveSessionController,
    val saveBookVerdictUseCase: SaveBookVerdictUseCase,
    val undoableBookRemover: UndoableBookRemover,
    val getDateStyleAsFlowUseCase: GetDateStyleAsFlowUseCase,
    val observeCurrentDateUseCase: ObserveCurrentDateUseCase,
    val setEditionAsOwnedUseCase: SetEditionAsOwnedUseCase,
    val getAllUserListsUseCase: GetAllUserListsUseCase,
    val addBookToListUseCase: AddBookToListUseCase,
    val removeBookFromListUseCase: RemoveBookFromListUseCase,
    val observeBookDeadlineUseCase: ObserveBookDeadlineUseCase,
    val setBookDeadlineUseCase: SetBookDeadlineUseCase,
    val clearBookDeadlineUseCase: ClearBookDeadlineUseCase,
    val getTopBookReviewsUseCase: GetTopBookReviewsUseCase,
    val observeReadingJournalHistoryUseCase: ObserveReadingJournalHistoryUseCase,
    val refreshReadingJournalHistoryUseCase: RefreshReadingJournalHistoryUseCase,
    val updateBookReviewUseCase: UpdateBookReviewUseCase,
    val observeUserProfileDataUseCase: ObserveUserProfileDataUseCase,
    val getUserTagsUseCase: GetUserTagsUseCase,
    val saveUserTagsUseCase: SaveUserTagsUseCase,
    val progressSheetSlice: ProgressSheetSlice,
    val observeUserTagVocabularyUseCase: ObserveUserTagVocabularyUseCase,
    val syncUserTagVocabularyUseCase: SyncUserTagVocabularyUseCase,
    val recordAppliedTagsUseCase: RecordAppliedTagsUseCase,
    val addQuoteUseCase: AddQuoteUseCase,
    val deleteQuoteUseCase: DeleteQuoteUseCase,
    val observeBookQuotesUseCase: ObserveBookQuotesUseCase,
    val refreshBookQuotesUseCase: RefreshBookQuotesUseCase,
    val loadSimilarBooksPageUseCase: LoadSimilarBooksPageUseCase,
    val observeShelvedBookIdsUseCase: ObserveShelvedBookIdsUseCase,
    val offlineCopy: OfflineCopy,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()

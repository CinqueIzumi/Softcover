package nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.book.domain.usecase.FetchBookByIdUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetEditionsByBookIdUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsReadingUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsWantToReadUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RecordBookProgressUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RemoveBookFromLibraryUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.UpdateBookEditionUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.UpdateBookRatingUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.UpdateBookReviewUseCase
import nl.rhaydus.softcover.core.deadlines.domain.usecase.ClearBookDeadlineUseCase
import nl.rhaydus.softcover.core.deadlines.domain.usecase.ObserveBookDeadlineUseCase
import nl.rhaydus.softcover.core.deadlines.domain.usecase.SetBookDeadlineUseCase
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionDependencies
import nl.rhaydus.softcover.core.lists.domain.usecase.AddBookToListUseCase
import nl.rhaydus.softcover.core.lists.domain.usecase.GetAllUserListsUseCase
import nl.rhaydus.softcover.core.lists.domain.usecase.RemoveBookFromListUseCase
import nl.rhaydus.softcover.core.lists.domain.usecase.SetEditionAsOwnedUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetDateStyleAsFlowUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveUserProfileDataUseCase
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.GetTopBookReviewsUseCase
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.GetUserTagsUseCase
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.SaveUserTagsUseCase

internal class BookDetailDependencies(
    val recordBookProgressUseCase: RecordBookProgressUseCase,
    val updateBookEditionUseCase: UpdateBookEditionUseCase,
    val fetchBookByIdUseCase: FetchBookByIdUseCase,
    val getEditionsByBookIdUseCase: GetEditionsByBookIdUseCase,
    val getAllUserBooksUseCase: GetAllUserBooksUseCase,
    val markBookAsWantToReadUseCase: MarkBookAsWantToReadUseCase,
    val markBookAsReadingUseCase: MarkBookAsReadingUseCase,
    val markBookAsReadUseCase: MarkBookAsReadUseCase,
    val updateBookRatingUseCase: UpdateBookRatingUseCase,
    val removeBookFromLibraryUseCase: RemoveBookFromLibraryUseCase,
    val getDateStyleAsFlowUseCase: GetDateStyleAsFlowUseCase,
    val setEditionAsOwnedUseCase: SetEditionAsOwnedUseCase,
    val getAllUserListsUseCase: GetAllUserListsUseCase,
    val addBookToListUseCase: AddBookToListUseCase,
    val removeBookFromListUseCase: RemoveBookFromListUseCase,
    val observeBookDeadlineUseCase: ObserveBookDeadlineUseCase,
    val setBookDeadlineUseCase: SetBookDeadlineUseCase,
    val clearBookDeadlineUseCase: ClearBookDeadlineUseCase,
    val getTopBookReviewsUseCase: GetTopBookReviewsUseCase,
    val updateBookReviewUseCase: UpdateBookReviewUseCase,
    val observeUserProfileDataUseCase: ObserveUserProfileDataUseCase,
    val getUserTagsUseCase: GetUserTagsUseCase,
    val saveUserTagsUseCase: SaveUserTagsUseCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()

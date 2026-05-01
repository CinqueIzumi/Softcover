package nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.presentation.toad.ActionDependencies
import nl.rhaydus.softcover.feature.books.domain.usecase.FetchBookByIdUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.GetEditionsByBookIdUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.MarkBookAsReadingUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.MarkBookAsWantToReadUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.SetEditionAsOwnedUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.RemoveBookFromLibraryUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.UpdateBookEditionUseCase
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.GetTopBookReviewsUseCase
import nl.rhaydus.softcover.feature.deadlines.domain.usecase.ClearBookDeadlineUseCase
import nl.rhaydus.softcover.feature.deadlines.domain.usecase.ObserveBookDeadlineUseCase
import nl.rhaydus.softcover.feature.deadlines.domain.usecase.SetBookDeadlineUseCase
import nl.rhaydus.softcover.feature.reading.presentation.util.UpdateBookProgress
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetDateStyleAsFlowUseCase

class BookDetailDependencies(
    val updateBookProgress: UpdateBookProgress,
    val updateBookEditionUseCase: UpdateBookEditionUseCase,
    val fetchBookByIdUseCase: FetchBookByIdUseCase,
    val getEditionsByBookIdUseCase: GetEditionsByBookIdUseCase,
    val getAllUserBooksUseCase: GetAllUserBooksUseCase,
    val markBookAsWantToReadUseCase: MarkBookAsWantToReadUseCase,
    val markBookAsReadingUseCase: MarkBookAsReadingUseCase,
    val markBookAsReadUseCase: MarkBookAsReadUseCase,
    val removeBookFromLibraryUseCase: RemoveBookFromLibraryUseCase,
    val getDateStyleAsFlowUseCase: GetDateStyleAsFlowUseCase,
    val setEditionAsOwnedUseCase: SetEditionAsOwnedUseCase,
    val observeBookDeadlineUseCase: ObserveBookDeadlineUseCase,
    val setBookDeadlineUseCase: SetBookDeadlineUseCase,
    val clearBookDeadlineUseCase: ClearBookDeadlineUseCase,
    val getTopBookReviewsUseCase: GetTopBookReviewsUseCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()
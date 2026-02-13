package nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.presentation.toad.ActionDependencies
import nl.rhaydus.softcover.feature.books.domain.usecase.FetchBookByIdUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.MarkBookAsReadingUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.MarkBookAsWantToReadUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.RemoveBookFromLibraryUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.UpdateBookEditionUseCase
import nl.rhaydus.softcover.feature.reading.presentation.util.UpdateBookProgress

class BookDetailDependencies(
    val updateBookProgress: UpdateBookProgress,
    val updateBookEditionUseCase: UpdateBookEditionUseCase,
    val fetchBookByIdUseCase: FetchBookByIdUseCase,
    val getAllUserBooksUseCase: GetAllUserBooksUseCase,
    val markBookAsWantToReadUseCase: MarkBookAsWantToReadUseCase,
    val markBookAsReadingUseCase: MarkBookAsReadingUseCase,
    val removeBookFromLibraryUseCase: RemoveBookFromLibraryUseCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()
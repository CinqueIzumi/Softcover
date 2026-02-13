package nl.rhaydus.softcover.feature.book.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.presentation.toad.ActionDependencies
import nl.rhaydus.softcover.feature.book.domain.usecase.FetchBookByIdUseCase
import nl.rhaydus.softcover.feature.book.domain.usecase.MarkBookAsReadingUseCase
import nl.rhaydus.softcover.feature.book.domain.usecase.MarkBookAsWantToReadUseCase
import nl.rhaydus.softcover.feature.book.domain.usecase.RemoveBookFromLibraryUseCase
import nl.rhaydus.softcover.feature.caching.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.UpdateBookEditionUseCase
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
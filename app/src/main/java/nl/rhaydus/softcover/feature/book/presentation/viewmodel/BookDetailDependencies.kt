package nl.rhaydus.softcover.feature.book.presentation.viewmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.presentation.toad.ActionDependencies
import nl.rhaydus.softcover.feature.book.domain.usecase.FetchBookByIdUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.UpdateBookEditionUseCase
import nl.rhaydus.softcover.feature.reading.presentation.util.UpdateBookProgress

class BookDetailDependencies(
    val updateBookProgress: UpdateBookProgress,
    val updateBookEditionUseCase: UpdateBookEditionUseCase,
    val fetchBookByIdUseCase: FetchBookByIdUseCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()
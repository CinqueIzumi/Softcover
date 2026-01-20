package nl.rhaydus.softcover.feature.reading.presentation.viewmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.presentation.toad.ActionDependencies
import nl.rhaydus.softcover.feature.reading.domain.usecase.GetCurrentlyReadingBooksUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.RefreshCurrentlyReadingBooksUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.UpdateBookEditionUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.UpdateBookProgressUseCase
import nl.rhaydus.softcover.feature.reading.presentation.util.UpdateBookProgressUtil

data class ReadingScreenDependencies(
    val getCurrentlyReadingBooksUseCase: GetCurrentlyReadingBooksUseCase,
    val updateBookProgressUseCase: UpdateBookProgressUseCase,
    val markBookAsReadUseCase: MarkBookAsReadUseCase,
    val refreshCurrentlyReadingBooksUseCase: RefreshCurrentlyReadingBooksUseCase,
    val updateBookEditionUseCase: UpdateBookEditionUseCase,
    val updateBookProgressUtil: UpdateBookProgressUtil,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()
package nl.rhaydus.softcover.feature.reading.presentation.viewmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.presentation.toad.ActionDependencies
import nl.rhaydus.softcover.feature.caching.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.feature.caching.domain.usecase.InitializeUserBooksUseCase
import nl.rhaydus.softcover.feature.caching.domain.usecase.RefreshUserBooksUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.UpdateBookEditionUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.UpdateBookProgressUseCase
import nl.rhaydus.softcover.feature.reading.presentation.util.UpdateBookProgress

data class ReadingScreenDependencies(
    val getCurrentlyReadingBooksUseCase: GetCurrentlyReadingUserBooksUseCase,
    val updateBookProgressUseCase: UpdateBookProgressUseCase,
    val markBookAsReadUseCase: MarkBookAsReadUseCase,
    val refreshUserBooksUseCase: RefreshUserBooksUseCase,
    val updateBookEditionUseCase: UpdateBookEditionUseCase,
    val updateBookProgress: UpdateBookProgress,
    val initializeUserBooksUseCase: InitializeUserBooksUseCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()
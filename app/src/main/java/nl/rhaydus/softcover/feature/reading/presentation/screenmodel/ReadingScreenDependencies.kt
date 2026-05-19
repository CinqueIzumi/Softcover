package nl.rhaydus.softcover.feature.reading.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.presentation.toad.ActionDependencies
import nl.rhaydus.softcover.feature.books.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.GetWantToReadUserBooksUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.RefreshUserBooksUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.UpdateBookProgressUseCase
import nl.rhaydus.softcover.feature.deadlines.domain.usecase.ObserveAllBookDeadlinesUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetTrendingBooksUseCase
import nl.rhaydus.softcover.feature.reading.presentation.util.UpdateBookProgress
import nl.rhaydus.softcover.feature.settings.domain.usecase.DismissPlanTodayUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetDateStyleAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.ObservePlanTodayDismissalsUseCase

data class ReadingScreenDependencies(
    val getCurrentlyReadingBooksUseCase: GetCurrentlyReadingUserBooksUseCase,
    val updateBookProgressUseCase: UpdateBookProgressUseCase,
    val markBookAsReadUseCase: MarkBookAsReadUseCase,
    val refreshUserBooksUseCase: RefreshUserBooksUseCase,
    val updateBookProgress: UpdateBookProgress,
    val observeAllBookDeadlinesUseCase: ObserveAllBookDeadlinesUseCase,
    val getDateStyleAsFlowUseCase: GetDateStyleAsFlowUseCase,
    val observePlanTodayDismissalsUseCase: ObservePlanTodayDismissalsUseCase,
    val dismissPlanTodayUseCase: DismissPlanTodayUseCase,
    val getWantToReadUserBooksUseCase: GetWantToReadUserBooksUseCase,
    val getTrendingBooksUseCase: GetTrendingBooksUseCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()
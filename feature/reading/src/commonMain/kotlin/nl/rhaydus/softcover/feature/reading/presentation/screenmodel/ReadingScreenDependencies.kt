package nl.rhaydus.softcover.feature.reading.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.book.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetTrendingBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetWantToReadUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RecordBookProgressUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.UpdateBookProgressUseCase
import nl.rhaydus.softcover.core.deadlines.domain.usecase.ObserveAllBookDeadlinesUseCase
import nl.rhaydus.softcover.core.library.domain.usecase.RefreshLibraryUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.DismissPlanTodayUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetDateStyleAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetReadingStreakEnabledAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.ObservePlanTodayDismissalsUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveRecentReadingActivityUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.RefreshUserProfileDataUseCase
import nl.rhaydus.toad.ActionDependencies

internal data class ReadingScreenDependencies(
    val getCurrentlyReadingBooksUseCase: GetCurrentlyReadingUserBooksUseCase,
    val updateBookProgressUseCase: UpdateBookProgressUseCase,
    val markBookAsReadUseCase: MarkBookAsReadUseCase,
    val refreshLibraryUseCase: RefreshLibraryUseCase,
    val recordBookProgressUseCase: RecordBookProgressUseCase,
    val observeAllBookDeadlinesUseCase: ObserveAllBookDeadlinesUseCase,
    val getDateStyleAsFlowUseCase: GetDateStyleAsFlowUseCase,
    val observePlanTodayDismissalsUseCase: ObservePlanTodayDismissalsUseCase,
    val dismissPlanTodayUseCase: DismissPlanTodayUseCase,
    val getWantToReadUserBooksUseCase: GetWantToReadUserBooksUseCase,
    val getTrendingBooksUseCase: GetTrendingBooksUseCase,
    val observeRecentReadingActivityUseCase: ObserveRecentReadingActivityUseCase,
    val refreshUserProfileDataUseCase: RefreshUserProfileDataUseCase,
    val getReadingStreakEnabledAsFlowUseCase: GetReadingStreakEnabledAsFlowUseCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()

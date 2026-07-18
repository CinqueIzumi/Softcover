package nl.rhaydus.softcover.feature.reading.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetTrendingBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetWantToReadUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RecordBookProgressUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.UpdateBookProgressUseCase
import nl.rhaydus.softcover.core.deadlines.domain.usecase.ObserveAllBookDeadlinesUseCase
import nl.rhaydus.softcover.core.domain.account.RefreshLibraryUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.GetReadingJournalHistoryUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.DismissPlanTodayUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetDateStyleAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetLastUsedProgressUnitAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetReadingStreakEnabledAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.ObservePlanTodayDismissalsUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetLastUsedProgressUnitUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveRecentReadingActivityUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.RefreshUserProfileDataUseCase
import nl.rhaydus.softcover.feature.reading.presentation.action.ReadingAction
import nl.rhaydus.softcover.feature.reading.presentation.collector.ReadingCollector
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ToadScreenModel

internal class ReadingScreenScreenModel(
    private val getCurrentlyReadingBooksUseCase: GetCurrentlyReadingUserBooksUseCase,
    private val updateBookProgressUseCase: UpdateBookProgressUseCase,
    private val markBookAsReadUseCase: MarkBookAsReadUseCase,
    private val refreshLibraryUseCase: RefreshLibraryUseCase,
    private val recordBookProgressUseCase: RecordBookProgressUseCase,
    private val observeAllBookDeadlinesUseCase: ObserveAllBookDeadlinesUseCase,
    private val getDateStyleAsFlowUseCase: GetDateStyleAsFlowUseCase,
    private val observePlanTodayDismissalsUseCase: ObservePlanTodayDismissalsUseCase,
    private val dismissPlanTodayUseCase: DismissPlanTodayUseCase,
    private val getWantToReadUserBooksUseCase: GetWantToReadUserBooksUseCase,
    private val getTrendingBooksUseCase: GetTrendingBooksUseCase,
    private val observeRecentReadingActivityUseCase: ObserveRecentReadingActivityUseCase,
    private val refreshUserProfileDataUseCase: RefreshUserProfileDataUseCase,
    private val getReadingStreakEnabledAsFlowUseCase: GetReadingStreakEnabledAsFlowUseCase,
    private val getLastUsedProgressUnitAsFlowUseCase: GetLastUsedProgressUnitAsFlowUseCase,
    private val setLastUsedProgressUnitUseCase: SetLastUsedProgressUnitUseCase,
    private val getReadingJournalHistoryUseCase: GetReadingJournalHistoryUseCase,
    appDispatchers: AppDispatchers,
    flows: List<ReadingCollector>,
) : ToadScreenModel<ReadingScreenUiState, ReadingScreenEvent, ReadingScreenDependencies, ReadingCollector, ReadingLocalVariables>(
    initialState = ReadingScreenUiState(),
    initialLocalVariables = ReadingLocalVariables(),
    initializers = flows,
) {
    override val dependencies = ReadingScreenDependencies(
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
        getCurrentlyReadingBooksUseCase = getCurrentlyReadingBooksUseCase,
        updateBookProgressUseCase = updateBookProgressUseCase,
        markBookAsReadUseCase = markBookAsReadUseCase,
        refreshLibraryUseCase = refreshLibraryUseCase,
        recordBookProgressUseCase = recordBookProgressUseCase,
        observeAllBookDeadlinesUseCase = observeAllBookDeadlinesUseCase,
        getDateStyleAsFlowUseCase = getDateStyleAsFlowUseCase,
        observePlanTodayDismissalsUseCase = observePlanTodayDismissalsUseCase,
        dismissPlanTodayUseCase = dismissPlanTodayUseCase,
        getWantToReadUserBooksUseCase = getWantToReadUserBooksUseCase,
        getTrendingBooksUseCase = getTrendingBooksUseCase,
        observeRecentReadingActivityUseCase = observeRecentReadingActivityUseCase,
        refreshUserProfileDataUseCase = refreshUserProfileDataUseCase,
        getReadingStreakEnabledAsFlowUseCase = getReadingStreakEnabledAsFlowUseCase,
        getLastUsedProgressUnitAsFlowUseCase = getLastUsedProgressUnitAsFlowUseCase,
        setLastUsedProgressUnitUseCase = setLastUsedProgressUnitUseCase,
        getReadingJournalHistoryUseCase = getReadingJournalHistoryUseCase,
    )

    init {
        startInitializers()
    }

    fun runAction(action: ReadingAction) = dispatch(action)
}

package nl.rhaydus.softcover.feature.reading.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetPausedUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetWantToReadUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveTrendingBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RecordBookProgressUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RefreshTrendingBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.SaveBookVerdictUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.UpdateBookProgressUseCase
import nl.rhaydus.softcover.core.deadlines.domain.usecase.ObserveAllBookDeadlinesUseCase
import nl.rhaydus.softcover.core.domain.account.RefreshLibraryUseCase
import nl.rhaydus.softcover.core.domain.util.ObserveCurrentDateUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.ObserveReadingJournalHistoryUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.RefreshReadingJournalHistoryUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.DismissPlanTodayUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetDateStyleAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetLastUsedProgressUnitAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetReadingStreakEnabledAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.ObservePlanTodayDismissalsUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetLastUsedProgressUnitUseCase
import nl.rhaydus.softcover.core.presentation.session.ActiveSessionController
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveRecentReadingActivityUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.RefreshReadingActivityUseCase
import nl.rhaydus.softcover.feature.reading.presentation.action.ReadingAction
import nl.rhaydus.softcover.feature.reading.presentation.collector.ReadingCollector
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ToadScreenModel

internal class ReadingScreenScreenModel(
    getCurrentlyReadingBooksUseCase: GetCurrentlyReadingUserBooksUseCase,
    updateBookProgressUseCase: UpdateBookProgressUseCase,
    markBookAsReadUseCase: MarkBookAsReadUseCase,
    saveBookVerdictUseCase: SaveBookVerdictUseCase,
    refreshLibraryUseCase: RefreshLibraryUseCase,
    recordBookProgressUseCase: RecordBookProgressUseCase,
    observeAllBookDeadlinesUseCase: ObserveAllBookDeadlinesUseCase,
    observeCurrentDateUseCase: ObserveCurrentDateUseCase,
    getDateStyleAsFlowUseCase: GetDateStyleAsFlowUseCase,
    observePlanTodayDismissalsUseCase: ObservePlanTodayDismissalsUseCase,
    dismissPlanTodayUseCase: DismissPlanTodayUseCase,
    getWantToReadUserBooksUseCase: GetWantToReadUserBooksUseCase,
    getPausedUserBooksUseCase: GetPausedUserBooksUseCase,
    observeTrendingBooksUseCase: ObserveTrendingBooksUseCase,
    refreshTrendingBooksUseCase: RefreshTrendingBooksUseCase,
    observeRecentReadingActivityUseCase: ObserveRecentReadingActivityUseCase,
    refreshReadingActivityUseCase: RefreshReadingActivityUseCase,
    getReadingStreakEnabledAsFlowUseCase: GetReadingStreakEnabledAsFlowUseCase,
    getLastUsedProgressUnitAsFlowUseCase: GetLastUsedProgressUnitAsFlowUseCase,
    setLastUsedProgressUnitUseCase: SetLastUsedProgressUnitUseCase,
    observeReadingJournalHistoryUseCase: ObserveReadingJournalHistoryUseCase,
    refreshReadingJournalHistoryUseCase: RefreshReadingJournalHistoryUseCase,
    activeSessionController: ActiveSessionController,
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
        activeSessionController = activeSessionController,
        getCurrentlyReadingBooksUseCase = getCurrentlyReadingBooksUseCase,
        updateBookProgressUseCase = updateBookProgressUseCase,
        markBookAsReadUseCase = markBookAsReadUseCase,
        saveBookVerdictUseCase = saveBookVerdictUseCase,
        refreshLibraryUseCase = refreshLibraryUseCase,
        recordBookProgressUseCase = recordBookProgressUseCase,
        observeAllBookDeadlinesUseCase = observeAllBookDeadlinesUseCase,
        observeCurrentDateUseCase = observeCurrentDateUseCase,
        getDateStyleAsFlowUseCase = getDateStyleAsFlowUseCase,
        observePlanTodayDismissalsUseCase = observePlanTodayDismissalsUseCase,
        dismissPlanTodayUseCase = dismissPlanTodayUseCase,
        getWantToReadUserBooksUseCase = getWantToReadUserBooksUseCase,
        getPausedUserBooksUseCase = getPausedUserBooksUseCase,
        observeTrendingBooksUseCase = observeTrendingBooksUseCase,
        refreshTrendingBooksUseCase = refreshTrendingBooksUseCase,
        observeRecentReadingActivityUseCase = observeRecentReadingActivityUseCase,
        refreshReadingActivityUseCase = refreshReadingActivityUseCase,
        getReadingStreakEnabledAsFlowUseCase = getReadingStreakEnabledAsFlowUseCase,
        getLastUsedProgressUnitAsFlowUseCase = getLastUsedProgressUnitAsFlowUseCase,
        setLastUsedProgressUnitUseCase = setLastUsedProgressUnitUseCase,
        observeReadingJournalHistoryUseCase = observeReadingJournalHistoryUseCase,
        refreshReadingJournalHistoryUseCase = refreshReadingJournalHistoryUseCase,
    )

    init {
        startInitializers()
    }

    fun runAction(action: ReadingAction) = dispatch(action)
}

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
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetReadingStreakEnabledAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.ObservePlanTodayDismissalsUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveRecentReadingActivityUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.RefreshReadingActivityUseCase
import nl.rhaydus.softcover.core.progresssheet.presentation.ProgressSheetSlice
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
    private val saveBookVerdictUseCase: SaveBookVerdictUseCase,
    private val refreshLibraryUseCase: RefreshLibraryUseCase,
    private val recordBookProgressUseCase: RecordBookProgressUseCase,
    private val observeAllBookDeadlinesUseCase: ObserveAllBookDeadlinesUseCase,
    private val observeCurrentDateUseCase: ObserveCurrentDateUseCase,
    private val getDateStyleAsFlowUseCase: GetDateStyleAsFlowUseCase,
    private val observePlanTodayDismissalsUseCase: ObservePlanTodayDismissalsUseCase,
    private val dismissPlanTodayUseCase: DismissPlanTodayUseCase,
    private val getWantToReadUserBooksUseCase: GetWantToReadUserBooksUseCase,
    private val getPausedUserBooksUseCase: GetPausedUserBooksUseCase,
    private val observeTrendingBooksUseCase: ObserveTrendingBooksUseCase,
    private val refreshTrendingBooksUseCase: RefreshTrendingBooksUseCase,
    private val observeRecentReadingActivityUseCase: ObserveRecentReadingActivityUseCase,
    private val refreshReadingActivityUseCase: RefreshReadingActivityUseCase,
    private val getReadingStreakEnabledAsFlowUseCase: GetReadingStreakEnabledAsFlowUseCase,
    private val progressSheetSlice: ProgressSheetSlice,
    private val observeReadingJournalHistoryUseCase: ObserveReadingJournalHistoryUseCase,
    private val refreshReadingJournalHistoryUseCase: RefreshReadingJournalHistoryUseCase,
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
        progressSheetSlice = progressSheetSlice,
        observeReadingJournalHistoryUseCase = observeReadingJournalHistoryUseCase,
        refreshReadingJournalHistoryUseCase = refreshReadingJournalHistoryUseCase,
    )

    init {
        startInitializers()
    }

    fun runAction(action: ReadingAction) = dispatch(action)
}

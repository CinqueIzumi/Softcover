package nl.rhaydus.softcover.feature.profile.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveBooksByIdsUseCase
import nl.rhaydus.softcover.core.domain.util.ObserveCurrentDateUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetHideUntaggedAuthorsAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.ObserveReadDatesBackfilledUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.ObserveReadingStatsBackfilledUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetHideUntaggedAuthorsUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveHourlyPagesReadUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveMonthlyPagesReadUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingActivityUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingDayUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingHistoryUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingStatsUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveTimeToFinishUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveUserProfileDataUseCase
import nl.rhaydus.softcover.feature.profile.presentation.action.StatsAtlasAction
import nl.rhaydus.softcover.feature.profile.presentation.collector.StatsAtlasCollector
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ToadScreenModel

internal class StatsAtlasScreenModel(
    observeUserProfileDataUseCase: ObserveUserProfileDataUseCase,
    observeReadingStatsUseCase: ObserveReadingStatsUseCase,
    observeMonthlyPagesReadUseCase: ObserveMonthlyPagesReadUseCase,
    observeHourlyPagesReadUseCase: ObserveHourlyPagesReadUseCase,
    observeReadingActivityUseCase: ObserveReadingActivityUseCase,
    observeReadingDayUseCase: ObserveReadingDayUseCase,
    observeReadingHistoryUseCase: ObserveReadingHistoryUseCase,
    observeReadingStatsBackfilledUseCase: ObserveReadingStatsBackfilledUseCase,
    observeReadDatesBackfilledUseCase: ObserveReadDatesBackfilledUseCase,
    observeTimeToFinishUseCase: ObserveTimeToFinishUseCase,
    getHideUntaggedAuthorsAsFlowUseCase: GetHideUntaggedAuthorsAsFlowUseCase,
    setHideUntaggedAuthorsUseCase: SetHideUntaggedAuthorsUseCase,
    observeBooksByIdsUseCase: ObserveBooksByIdsUseCase,
    observeCurrentDateUseCase: ObserveCurrentDateUseCase,
    dispatchers: AppDispatchers,
    initializers: List<StatsAtlasCollector>,
) : ToadScreenModel<
        StatsAtlasUiState,
        StatsAtlasEvent,
        StatsAtlasDependencies,
        StatsAtlasCollector,
        StatsAtlasLocalVariables,
    >(
        initializers = initializers,
        initialState = StatsAtlasUiState(),
        initialLocalVariables = StatsAtlasLocalVariables(),
    ) {
    override val dependencies: StatsAtlasDependencies = StatsAtlasDependencies(
        observeUserProfileDataUseCase = observeUserProfileDataUseCase,
        observeReadingStatsUseCase = observeReadingStatsUseCase,
        observeMonthlyPagesReadUseCase = observeMonthlyPagesReadUseCase,
        observeHourlyPagesReadUseCase = observeHourlyPagesReadUseCase,
        observeReadingActivityUseCase = observeReadingActivityUseCase,
        observeReadingDayUseCase = observeReadingDayUseCase,
        observeReadingHistoryUseCase = observeReadingHistoryUseCase,
        observeReadingStatsBackfilledUseCase = observeReadingStatsBackfilledUseCase,
        observeReadDatesBackfilledUseCase = observeReadDatesBackfilledUseCase,
        observeTimeToFinishUseCase = observeTimeToFinishUseCase,
        getHideUntaggedAuthorsAsFlowUseCase = getHideUntaggedAuthorsAsFlowUseCase,
        setHideUntaggedAuthorsUseCase = setHideUntaggedAuthorsUseCase,
        observeBooksByIdsUseCase = observeBooksByIdsUseCase,
        observeCurrentDateUseCase = observeCurrentDateUseCase,
        mainDispatcher = dispatchers.main,
        coroutineScope = screenModelScope,
    )

    init {
        startInitializers()
    }

    fun runAction(action: StatsAtlasAction) = dispatch(action)
}

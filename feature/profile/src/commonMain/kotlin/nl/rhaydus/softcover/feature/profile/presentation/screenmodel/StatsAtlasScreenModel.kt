package nl.rhaydus.softcover.feature.profile.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveBooksByIdsUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetHideUntaggedAuthorsAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetHideUntaggedAuthorsUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingLifeUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveUserProfileDataUseCase
import nl.rhaydus.softcover.feature.profile.presentation.action.StatsAtlasAction
import nl.rhaydus.softcover.feature.profile.presentation.collector.StatsAtlasCollector
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ToadScreenModel

internal class StatsAtlasScreenModel(
    private val observeUserProfileDataUseCase: ObserveUserProfileDataUseCase,
    private val observeReadingLifeUseCase: ObserveReadingLifeUseCase,
    private val getHideUntaggedAuthorsAsFlowUseCase: GetHideUntaggedAuthorsAsFlowUseCase,
    private val setHideUntaggedAuthorsUseCase: SetHideUntaggedAuthorsUseCase,
    private val observeBooksByIdsUseCase: ObserveBooksByIdsUseCase,
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
        observeReadingLifeUseCase = observeReadingLifeUseCase,
        getHideUntaggedAuthorsAsFlowUseCase = getHideUntaggedAuthorsAsFlowUseCase,
        setHideUntaggedAuthorsUseCase = setHideUntaggedAuthorsUseCase,
        observeBooksByIdsUseCase = observeBooksByIdsUseCase,
        mainDispatcher = dispatchers.main,
        coroutineScope = screenModelScope,
    )

    init {
        startInitializers()
    }

    fun runAction(action: StatsAtlasAction) = dispatch(action)
}

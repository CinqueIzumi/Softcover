package nl.rhaydus.softcover.feature.explore.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.feature.explore.domain.usecase.DismissContinueSeriesBookUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.DismissContinueSeriesUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.EnrichDismissedContinueSeriesMetadataUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetDismissedContinueSeriesBooksUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetDismissedContinueSeriesUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.UndoContinueSeriesBookDismissalUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.UndoContinueSeriesDismissalUseCase
import nl.rhaydus.softcover.feature.explore.presentation.action.HiddenSuggestionsAction
import nl.rhaydus.softcover.feature.explore.presentation.collector.HiddenSuggestionsCollector
import nl.rhaydus.softcover.feature.explore.presentation.event.HiddenSuggestionsEvent
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsUiState
import nl.rhaydus.toad.ToadScreenModel

internal class HiddenSuggestionsScreenModel(
    private val getDismissedContinueSeriesBooksUseCase: GetDismissedContinueSeriesBooksUseCase,
    private val getDismissedContinueSeriesUseCase: GetDismissedContinueSeriesUseCase,
    private val undoContinueSeriesBookDismissalUseCase: UndoContinueSeriesBookDismissalUseCase,
    private val undoContinueSeriesDismissalUseCase: UndoContinueSeriesDismissalUseCase,
    private val dismissContinueSeriesBookUseCase: DismissContinueSeriesBookUseCase,
    private val dismissContinueSeriesUseCase: DismissContinueSeriesUseCase,
    private val enrichDismissedContinueSeriesMetadataUseCase: EnrichDismissedContinueSeriesMetadataUseCase,
    appDispatchers: AppDispatchers,
    flows: List<HiddenSuggestionsCollector>,
) : ToadScreenModel<
    HiddenSuggestionsUiState,
    HiddenSuggestionsEvent,
    HiddenSuggestionsDependencies,
    HiddenSuggestionsCollector,
    HiddenSuggestionsLocalVariables,
    >(
    initialState = HiddenSuggestionsUiState(),
    initialLocalVariables = HiddenSuggestionsLocalVariables(),
    initializers = flows,
) {
    override val dependencies = HiddenSuggestionsDependencies(
        getDismissedContinueSeriesBooksUseCase = getDismissedContinueSeriesBooksUseCase,
        getDismissedContinueSeriesUseCase = getDismissedContinueSeriesUseCase,
        undoContinueSeriesBookDismissalUseCase = undoContinueSeriesBookDismissalUseCase,
        undoContinueSeriesDismissalUseCase = undoContinueSeriesDismissalUseCase,
        dismissContinueSeriesBookUseCase = dismissContinueSeriesBookUseCase,
        dismissContinueSeriesUseCase = dismissContinueSeriesUseCase,
        enrichDismissedContinueSeriesMetadataUseCase = enrichDismissedContinueSeriesMetadataUseCase,
        mainDispatcher = appDispatchers.main,
        coroutineScope = screenModelScope,
    )

    init {
        startInitializers()
    }

    fun runAction(action: HiddenSuggestionsAction) = dispatch(action = action)
}

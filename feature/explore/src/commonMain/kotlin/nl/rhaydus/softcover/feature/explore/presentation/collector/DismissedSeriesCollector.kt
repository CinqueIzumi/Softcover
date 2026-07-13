package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.feature.explore.presentation.event.HiddenSuggestionsEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.HiddenSuggestionsDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsUiState
import nl.rhaydus.toad.ActionScope

internal class DismissedSeriesCollector : HiddenSuggestionsCollector {
    override suspend fun onLaunch(
        scope: ActionScope<HiddenSuggestionsUiState, HiddenSuggestionsEvent, HiddenSuggestionsLocalVariables>,
        dependencies: HiddenSuggestionsDependencies,
    ) {
        dependencies.getDismissedContinueSeriesUseCase().collectLatest { series ->
            scope.setState {
                it.copy(
                    hiddenSeries = series,
                    initialized = true,
                )
            }
        }
    }
}

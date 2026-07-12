package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.feature.explore.presentation.event.HiddenSuggestionsEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.HiddenSuggestionsDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsUiState
import nl.rhaydus.toad.ActionScope

internal class DismissedBooksCollector : HiddenSuggestionsCollector {
    override suspend fun onLaunch(
        scope: ActionScope<HiddenSuggestionsUiState, HiddenSuggestionsEvent, HiddenSuggestionsLocalVariables>,
        dependencies: HiddenSuggestionsDependencies,
    ) {
        dependencies.getDismissedContinueSeriesBooksUseCase().collectLatest { books ->
            scope.setState {
                it.copy(
                    hiddenBooks = books,
                    initialized = true,
                )
            }
        }
    }
}

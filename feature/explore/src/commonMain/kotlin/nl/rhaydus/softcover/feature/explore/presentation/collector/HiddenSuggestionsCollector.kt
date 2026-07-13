package nl.rhaydus.softcover.feature.explore.presentation.collector

import nl.rhaydus.softcover.feature.explore.presentation.event.HiddenSuggestionsEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.HiddenSuggestionsDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsUiState
import nl.rhaydus.toad.Collector

internal sealed interface HiddenSuggestionsCollector : Collector<
    HiddenSuggestionsUiState,
    HiddenSuggestionsEvent,
    HiddenSuggestionsDependencies,
    HiddenSuggestionsLocalVariables,
    >

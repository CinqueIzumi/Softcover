package nl.rhaydus.softcover.feature.explore.presentation.action

import nl.rhaydus.softcover.feature.explore.presentation.event.HiddenSuggestionsEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.HiddenSuggestionsDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsUiState
import nl.rhaydus.toad.UiAction

internal sealed interface HiddenSuggestionsAction : UiAction<
    HiddenSuggestionsDependencies,
    HiddenSuggestionsUiState,
    HiddenSuggestionsEvent,
    HiddenSuggestionsLocalVariables,
    >

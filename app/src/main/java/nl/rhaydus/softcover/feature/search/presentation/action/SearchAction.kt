package nl.rhaydus.softcover.feature.search.presentation.action

import nl.rhaydus.softcover.core.presentation.toad.UiAction
import nl.rhaydus.softcover.feature.search.presentation.event.SearchEvent
import nl.rhaydus.softcover.feature.search.presentation.state.SearchLocalVariables
import nl.rhaydus.softcover.feature.search.presentation.state.SearchScreenUiState
import nl.rhaydus.softcover.feature.search.presentation.screenmodel.SearchDependencies

sealed interface SearchAction : UiAction<
        SearchDependencies,
        SearchScreenUiState,
        SearchEvent,
        SearchLocalVariables
        >
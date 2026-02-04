package nl.rhaydus.softcover.feature.search.presentation.flows

import nl.rhaydus.softcover.core.presentation.toad.Initializer
import nl.rhaydus.softcover.feature.search.presentation.event.SearchEvent
import nl.rhaydus.softcover.feature.search.presentation.state.SearchLocalVariables
import nl.rhaydus.softcover.feature.search.presentation.state.SearchScreenUiState
import nl.rhaydus.softcover.feature.search.presentation.screenmodel.SearchDependencies

sealed interface SearchInitializer : Initializer<
        SearchScreenUiState,
        SearchEvent,
        SearchDependencies,
        SearchLocalVariables
        >
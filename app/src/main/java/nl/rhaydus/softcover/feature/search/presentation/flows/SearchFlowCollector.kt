package nl.rhaydus.softcover.feature.search.presentation.flows

import nl.rhaydus.softcover.core.presentation.toad.FlowCollector
import nl.rhaydus.softcover.feature.search.presentation.event.SearchEvent
import nl.rhaydus.softcover.feature.search.presentation.state.SearchLocalVariables
import nl.rhaydus.softcover.feature.search.presentation.state.SearchScreenUiState
import nl.rhaydus.softcover.feature.search.presentation.viewmodel.SearchDependencies

sealed interface SearchFlowCollector : FlowCollector<
        SearchScreenUiState,
        SearchEvent,
        SearchDependencies,
        SearchLocalVariables
        >
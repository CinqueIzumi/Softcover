package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps the search-results opener off [ExploreScreenUiState.queriedBooks], [ExploreScreenUiState.searchText]
 * and [ExploreScreenUiState.activeMoodFilter] (`component-contract.md` § 7.2 R9).
 */
internal class SearchResultsHeaderCollector : ExploreCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
        dependencies: ExploreDependencies,
    ) {
        scope.state
            .map {
                Triple(
                    it.queriedBooks.size,
                    it.searchText,
                    it.activeMoodFilter,
                )
            }
            .distinctUntilChanged()
            .collectLatest { (resultCount, searchText, activeMoodFilter) ->
                val header = SectionHeaderUiModel.Section(
                    eyebrow = "Showing $resultCount results",
                    description = when {
                        searchText.isNotEmpty() -> "for \"$searchText\""
                        activeMoodFilter != null -> "for \"${activeMoodFilter.label}\""
                        else -> null
                    },
                )

                scope.setState { state -> state.copy(searchResultsHeader = header) }
            }
    }
}

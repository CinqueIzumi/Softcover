package nl.rhaydus.softcover.feature.explore.presentation.state

import kotlinx.coroutines.Job
import nl.rhaydus.toad.LocalVariables

internal data class ExploreLocalVariables(
    val queryJob: Job? = null,
    /**
     * 1-based cursor for whichever search is currently active (text or mood - the two are
     * mutually exclusive, see [nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState]).
     * Reset to 1 whenever a fresh search starts, advanced by `OnLoadMoreSearchResultsAction`.
     */
    val searchResultsPage: Int = 1,
) : LocalVariables

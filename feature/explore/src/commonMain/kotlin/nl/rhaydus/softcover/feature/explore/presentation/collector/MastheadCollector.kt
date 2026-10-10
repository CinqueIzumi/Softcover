package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.header.PageMastheadSubtitle
import nl.rhaydus.softcover.core.component.header.PageMastheadUiModel
import nl.rhaydus.softcover.core.uibinding.masthead.mastheadRefreshFor
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps the desktop masthead and its refresh action off [ExploreScreenUiState.isRefreshing]
 * (`component-contract.md` § 7.2 R9).
 */
internal class MastheadCollector : ExploreCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
        dependencies: ExploreDependencies,
    ) {
        scope.state
            .map { it.isRefreshing }
            .distinctUntilChanged()
            .collectLatest { isRefreshing ->
                scope.setState { state ->
                    state.copy(
                        masthead = PageMastheadUiModel(
                            eyebrow = if (isRefreshing) "Finding fresh picks…" else "Discover",
                            title = "Explore",
                            subtitle = PageMastheadSubtitle.Text(
                                text = "Trending books, the next in your series, and anything you search for.",
                            ),
                        ),
                        mastheadRefresh = mastheadRefreshFor(isLoading = isRefreshing),
                    )
                }
            }
    }
}

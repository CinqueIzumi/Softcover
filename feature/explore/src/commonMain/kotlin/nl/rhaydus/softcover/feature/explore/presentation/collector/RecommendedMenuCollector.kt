package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.sheet.openOrUpdate
import nl.rhaydus.softcover.core.component.sheet.requestClose
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.mapper.recommendedMenuSheetFor
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

internal class RecommendedMenuCollector : ExploreCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
        dependencies: ExploreDependencies,
    ) {
        scope.state
            .map { state ->
                val book = state.recommendedBooks.find { it.id == state.recommendedMenuBookId }

                book?.let { it to state.recommendedHeader.eyebrow }
            }
            .distinctUntilChanged()
            .collectLatest { target ->
                scope.setState {
                    it.copy(
                        recommendedMenu = if (target == null || it.recommendedMenuBookId == null) {
                            it.recommendedMenu.requestClose()
                        } else {
                            it.recommendedMenu.openOrUpdate(
                                model = recommendedMenuSheetFor(
                                    book = target.first,
                                    eyebrow = target.second,
                                ),
                            )
                        },
                    )
                }
            }
    }
}

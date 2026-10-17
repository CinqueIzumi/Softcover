package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.sheet.openOrUpdate
import nl.rhaydus.softcover.core.component.sheet.requestClose
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.mapper.continueSeriesMenuSheetFor
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

internal class ContinueSeriesMenuCollector : ExploreCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
        dependencies: ExploreDependencies,
    ) {
        scope.state
            .map { state -> state.continueSeriesBooks.find { it.id == state.continueSeriesMenuBookId } }
            .distinctUntilChanged()
            .collectLatest { book ->
                scope.setState {
                    it.copy(
                        continueSeriesMenu = if (book == null || it.continueSeriesMenuBookId == null) {
                            it.continueSeriesMenu.requestClose()
                        } else {
                            it.continueSeriesMenu.openOrUpdate(model = continueSeriesMenuSheetFor(book = book))
                        },
                    )
                }
            }
    }
}

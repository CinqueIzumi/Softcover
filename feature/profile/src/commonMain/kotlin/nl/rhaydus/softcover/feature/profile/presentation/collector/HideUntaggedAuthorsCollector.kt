package nl.rhaydus.softcover.feature.profile.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.softcover.feature.profile.presentation.state.hideUntaggedAuthorsToggleFor
import nl.rhaydus.toad.ActionScope

internal class HideUntaggedAuthorsCollector : StatsAtlasCollector {
    override suspend fun onLaunch(
        scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>,
        dependencies: StatsAtlasDependencies,
    ) {
        dependencies.getHideUntaggedAuthorsAsFlowUseCase().collectLatest { hide ->
            scope.setState {
                it.copy(
                    hideUntaggedAuthors = hide,
                    hideUntaggedAuthorsToggle = hideUntaggedAuthorsToggleFor(hide = hide),
                )
            }
        }
    }
}

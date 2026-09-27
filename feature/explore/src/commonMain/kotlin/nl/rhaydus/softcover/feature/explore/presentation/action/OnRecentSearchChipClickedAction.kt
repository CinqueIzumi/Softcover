package nl.rhaydus.softcover.feature.explore.presentation.action

import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope
import kotlin.time.Duration.Companion.seconds

internal data class OnRecentSearchChipClickedAction(
    private val key: String,
) : ExploreAction {
    override suspend fun execute(
        dependencies: ExploreDependencies,
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
    ) {
        val query = scope.currentState.recentSearchQueryByChipKey[key] ?: return

        OnQueryChangeAction(
            newQuery = query,
            searchDelay = 0.seconds,
        ).execute(
            dependencies = dependencies,
            scope = scope,
        )
    }
}

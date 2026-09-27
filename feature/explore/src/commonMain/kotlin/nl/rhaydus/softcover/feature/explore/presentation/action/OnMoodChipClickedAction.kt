package nl.rhaydus.softcover.feature.explore.presentation.action

import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

internal data class OnMoodChipClickedAction(
    private val key: String,
) : ExploreAction {
    override suspend fun execute(
        dependencies: ExploreDependencies,
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
    ) {
        val mood = scope.currentState.moodChips[key] ?: return

        OnMoodChipClickAction(mood = mood).execute(
            dependencies = dependencies,
            scope = scope,
        )
    }
}

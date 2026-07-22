package nl.rhaydus.softcover.feature.explore.presentation.collector

import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

internal class MoodTagsCollector : ExploreCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
        dependencies: ExploreDependencies,
    ) {
        dependencies.getMoodTagsUseCase()
            .onFailure { error ->
                AppLog.e(
                    error,
                    "Failed to fetch mood tags",
                )
            }
            .onSuccess { tags ->
                scope.setState { it.copy(moodTags = tags) }
            }

        scope.setState { it.copy(loadingMoodTags = false) }
    }
}

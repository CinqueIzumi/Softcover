package nl.rhaydus.softcover.feature.explore.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.error.toUserMessage
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

internal suspend fun executeBookSearch(
    name: String,
    dependencies: ExploreDependencies,
    scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
) {
    dependencies.searchForNameUseCase(name = name)
        .onSuccess {
            scope.setState { it.copy(isLoading = false) }
        }
        .onFailure { error ->
            scope.setState {
                it.copy(
                    isLoading = false,
                    searchError = error.toUserMessage() ?: "We couldn't load results. Please try again.",
                )
            }
        }
}

package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

internal class BecauseYouReadCollector : ExploreCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
        dependencies: ExploreDependencies,
    ) {
        dependencies.getBecauseYouReadBooksUseCase().collectLatest { recommendation ->
            scope.setState {
                it.copy(
                    becauseYouReadGenre = recommendation?.genre,
                    becauseYouReadGenreOptions = recommendation?.genreOptions.orEmpty(),
                    becauseYouReadBooks = recommendation?.books.orEmpty(),
                    // Mapped from the recommendation rather than hardcoded false - see
                    // BecauseYouReadRecommendation.loading for why a mid-switch placeholder must
                    // keep this true instead of prematurely clearing OnBecauseYouReadGenreSelectedAction's
                    // optimistic loading state.
                    loadingBecauseYouReadBooks = recommendation?.loading ?: false,
                )
            }
        }
    }
}

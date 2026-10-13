package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.mapper.BECAUSE_YOU_READ_HEADER_STAND_IN
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
                    becauseYouReadHeader = recommendation?.genre?.let { genre ->
                        SectionHeaderUiModel.Section(
                            eyebrow = "Because you read",
                            headline = genre,
                            isLoading = recommendation.loading,
                        )
                    } ?: BECAUSE_YOU_READ_HEADER_STAND_IN,
                    becauseYouReadGenreOptions = recommendation?.genreOptions.orEmpty().toImmutableList(),
                    becauseYouReadBooks = recommendation?.books.orEmpty().toImmutableList(),
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

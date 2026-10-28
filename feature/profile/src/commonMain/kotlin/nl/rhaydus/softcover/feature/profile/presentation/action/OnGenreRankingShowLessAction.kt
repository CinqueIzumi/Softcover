package nl.rhaydus.softcover.feature.profile.presentation.action

import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.COLLAPSED_GENRE_COUNT
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope

/** Folds the ranking back to the top five; `GenreRankingCollector` rebuilds the ranking and its buttons from it. */
internal class OnGenreRankingShowLessAction : StatsAtlasAction {
    override suspend fun execute(
        dependencies: StatsAtlasDependencies,
        scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>,
    ) {
        scope.setState { it.copy(genreRankingShownCount = COLLAPSED_GENRE_COUNT) }
    }
}

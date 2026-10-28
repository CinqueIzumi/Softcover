package nl.rhaydus.softcover.feature.profile.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.profile.domain.model.GenreSlice
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genreHeader
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genreRankingShowLess
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genreRankingShowMore
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genreRankingUiModel
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope

/**
 * The only writer of the genre header, rows and show-more / show-less buttons. It maps the stats that have
 * landed rather than the selected scope, so the copy never describes a scope whose stats are still on their
 * way.
 */
internal class GenreRankingCollector : StatsAtlasCollector {
    override suspend fun onLaunch(
        scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>,
        dependencies: StatsAtlasDependencies,
    ) {
        scope.state
            .map { state ->
                GenreRankingSnapshot(
                    slices = state.readingStats?.genres?.slices,
                    shownCount = state.genreRankingShownCount,
                    emptyCaption = state.genreEmptyCaption,
                )
            }
            .distinctUntilChanged()
            .collectLatest { (slices, shownCount, emptyCaption) ->
                scope.setState {
                    it.copy(
                        genreHeader = genreHeader(
                            slices = slices,
                            emptyCaption = emptyCaption,
                        ),
                        genreRanking = genreRankingUiModel(
                            slices = slices,
                            shownCount = shownCount,
                        ),
                        genreRankingShowMore = genreRankingShowMore(
                            slices = slices,
                            shownCount = shownCount,
                        ),
                        genreRankingShowLess = genreRankingShowLess(
                            slices = slices,
                            shownCount = shownCount,
                        ),
                    )
                }
            }
    }

    private data class GenreRankingSnapshot(
        val slices: List<GenreSlice>?,
        val shownCount: Int,
        val emptyCaption: String,
    )
}

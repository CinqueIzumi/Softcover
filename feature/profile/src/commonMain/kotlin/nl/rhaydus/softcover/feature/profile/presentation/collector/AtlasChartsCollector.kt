package nl.rhaydus.softcover.feature.profile.presentation.collector

import kotlinx.coroutines.flow.combine
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.demographicBreakdownUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genderBreakdownUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genreRankingUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genreScopeCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.ratingsAverageUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.ratingsChartUiModel
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope

/**
 * The only writer of the genre, author and ratings charts and their captions.
 * They depend on the reading life, the loaded profile and whether untagged authors are hidden, so one
 * collector sees all three: the charts load until both the reading life and the profile have arrived,
 * and the hide switch is applied once for charts and captions alike.
 */
internal class AtlasChartsCollector : StatsAtlasCollector {
    override suspend fun onLaunch(
        scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>,
        dependencies: StatsAtlasDependencies,
    ) {
        combine(
            dependencies.observeReadingLifeUseCase(),
            dependencies.observeUserProfileDataUseCase(),
            dependencies.getHideUntaggedAuthorsAsFlowUseCase(),
        ) { readingLife, profileData, hideUntaggedAuthors ->
            if (readingLife == null || profileData == null) return@combine null

            val demographics = if (hideUntaggedAuthors) {
                readingLife.authorDemographics.excludingUntaggedAuthors()
            } else {
                readingLife.authorDemographics
            }

            ChartsInput(
                genres = readingLife.genres,
                demographics = demographics,
                ratings = readingLife.ratings,
                hideUntaggedAuthors = hideUntaggedAuthors,
            )
        }.collect { loaded ->
            val genres = loaded?.genres
            val demographics = loaded?.demographics
            val ratings = loaded?.ratings
            val hideUntaggedAuthors = loaded?.hideUntaggedAuthors ?: false

            scope.setState {
                it.copy(
                    genreRanking = genreRankingUiModel(genres),
                    hasGenres = genres?.slices.orEmpty().isNotEmpty(),
                    genreScopeCaption = genreScopeCaption(taggedBookCount = genres?.taggedBookCount ?: 0),
                    genderBreakdown = genderBreakdownUiModel(
                        demographics = demographics,
                        hideUntaggedAuthors = hideUntaggedAuthors,
                    ),
                    bipocBreakdown = demographicBreakdownUiModel(
                        breakdown = demographics?.bipocBreakdown,
                        hideUntaggedAuthors = hideUntaggedAuthors,
                    ),
                    lgbtqBreakdown = demographicBreakdownUiModel(
                        breakdown = demographics?.lgbtqBreakdown,
                        hideUntaggedAuthors = hideUntaggedAuthors,
                    ),
                    ratingsChart = ratingsChartUiModel(ratings),
                    ratingsAverage = ratingsAverageUiModel(ratings),
                )
            }
        }
    }
}

package nl.rhaydus.softcover.feature.profile.presentation.collector

import kotlinx.coroutines.flow.combine
import nl.rhaydus.softcover.core.profile.domain.model.AuthorDemographics
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.demographicChartUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genderChartUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genreRankingUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.ratingsAverageUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.ratingsChartUiModel
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.toad.ActionScope

/**
 * The only writer of the genre, author and ratings charts and of the author breakdown behind their captions.
 * They depend on the reading life, the loaded profile and whether untagged authors are hidden, so one
 * collector sees all three: the charts load until both the reading life and the profile have arrived,
 * and the hide switch is applied once for charts and captions alike.
 */
internal class ProfileChartsCollector : ProfileCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>,
        dependencies: ProfileDependencies,
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

            Triple(
                readingLife.genres,
                demographics,
                readingLife.ratings,
            )
        }.collect { loaded ->
            val genres = loaded?.first
            val demographics = loaded?.second
            val ratings = loaded?.third

            scope.setState {
                it.copy(
                    authorDemographics = demographics ?: AuthorDemographics(),
                    genreRanking = genreRankingUiModel(genres),
                    genderChart = genderChartUiModel(demographics?.genderSlices),
                    bipocChart = demographicChartUiModel(demographics?.bipocBreakdown),
                    lgbtqChart = demographicChartUiModel(demographics?.lgbtqBreakdown),
                    ratingsChart = ratingsChartUiModel(ratings),
                    ratingsAverage = ratingsAverageUiModel(ratings),
                )
            }
        }
    }
}

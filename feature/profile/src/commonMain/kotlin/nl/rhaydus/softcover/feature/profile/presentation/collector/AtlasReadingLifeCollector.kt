package nl.rhaydus.softcover.feature.profile.presentation.collector

import kotlinx.coroutines.flow.filterNotNull
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.ratingsAverageCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.toRatingsHistogramCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.toRatingsHistogramHeader
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope

internal class AtlasReadingLifeCollector : StatsAtlasCollector {
    override suspend fun onLaunch(
        scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>,
        dependencies: StatsAtlasDependencies,
    ) {
        dependencies.observeReadingLifeUseCase()
            .filterNotNull()
            .collect { readingLife ->
                scope.setState {
                    it.copy(
                        readingLife = readingLife,
                        ratingsHistogramHeader = readingLife.ratings.band.toRatingsHistogramHeader(),
                        ratingsHistogramCaption = readingLife.ratings.band.toRatingsHistogramCaption(),
                        ratingsAverageLabel = ratingsAverageCaption(totalRatings = readingLife.ratings.totalRatings),
                    )
                }
            }
    }
}

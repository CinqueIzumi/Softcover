package nl.rhaydus.softcover.feature.explore.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeries
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

class GetDismissedContinueSeriesUseCase(
    private val exploreRepository: ExploreRepository,
) {
    operator fun invoke(): Flow<List<DismissedSeries>> = exploreRepository.dismissedContinueSeries
}

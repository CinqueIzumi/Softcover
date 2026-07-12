package nl.rhaydus.softcover.feature.explore.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

class GetDismissedContinueSeriesBooksUseCase(
    private val exploreRepository: ExploreRepository,
) {
    operator fun invoke(): Flow<List<DismissedSeriesBook>> = exploreRepository.dismissedContinueSeriesBooks
}

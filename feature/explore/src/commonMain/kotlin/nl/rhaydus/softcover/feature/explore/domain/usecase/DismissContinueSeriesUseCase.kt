package nl.rhaydus.softcover.feature.explore.domain.usecase

import nl.rhaydus.softcover.core.domain.result.runCatchingLogged
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

class DismissContinueSeriesUseCase(
    private val exploreRepository: ExploreRepository,
) {
    suspend operator fun invoke(seriesId: Int): Result<Unit> = runCatchingLogged {
        exploreRepository.dismissContinueSeries(seriesId = seriesId)
    }
}

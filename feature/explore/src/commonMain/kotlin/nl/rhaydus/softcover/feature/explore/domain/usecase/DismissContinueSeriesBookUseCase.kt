package nl.rhaydus.softcover.feature.explore.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

class DismissContinueSeriesBookUseCase(
    private val exploreRepository: ExploreRepository,
) {
    suspend operator fun invoke(book: DismissedSeriesBook): Result<Unit> = runCatchingLogged {
        exploreRepository.dismissContinueSeriesBook(book = book)
    }
}

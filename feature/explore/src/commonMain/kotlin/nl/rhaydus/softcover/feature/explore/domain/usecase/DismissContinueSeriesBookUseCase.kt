package nl.rhaydus.softcover.feature.explore.domain.usecase

import nl.rhaydus.softcover.core.domain.result.runCatchingLogged
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

class DismissContinueSeriesBookUseCase(
    private val exploreRepository: ExploreRepository,
) {
    suspend operator fun invoke(bookId: Int): Result<Unit> = runCatchingLogged {
        exploreRepository.dismissContinueSeriesBook(bookId = bookId)
    }
}

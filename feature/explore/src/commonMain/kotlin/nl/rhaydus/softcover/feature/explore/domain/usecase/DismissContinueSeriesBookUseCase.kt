package nl.rhaydus.softcover.feature.explore.domain.usecase

import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

class DismissContinueSeriesBookUseCase(
    private val exploreRepository: ExploreRepository,
) {
    suspend operator fun invoke(bookId: Int): Result<Unit> = runCatching {
        exploreRepository.dismissContinueSeriesBook(bookId = bookId)
    }
}

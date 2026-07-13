package nl.rhaydus.softcover.feature.explore.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

class UndoContinueSeriesBookDismissalUseCase(
    private val exploreRepository: ExploreRepository,
) {
    suspend operator fun invoke(bookId: Int): Result<Unit> = runCatchingLogged {
        exploreRepository.undoContinueSeriesBookDismissal(bookId = bookId)
    }
}

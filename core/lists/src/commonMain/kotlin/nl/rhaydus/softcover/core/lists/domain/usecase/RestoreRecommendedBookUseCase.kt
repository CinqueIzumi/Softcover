package nl.rhaydus.softcover.core.lists.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.lists.domain.repository.HiddenRecommendationsRepository

class RestoreRecommendedBookUseCase(
    private val hiddenRecommendationsRepository: HiddenRecommendationsRepository,
) {
    suspend operator fun invoke(bookId: Int): Result<Unit> = runCatchingLogged {
        hiddenRecommendationsRepository.restore(bookId = bookId)
    }
}

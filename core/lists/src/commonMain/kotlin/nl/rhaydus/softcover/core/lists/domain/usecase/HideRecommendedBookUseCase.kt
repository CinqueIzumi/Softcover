package nl.rhaydus.softcover.core.lists.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.lists.domain.model.HiddenRecommendation
import nl.rhaydus.softcover.core.lists.domain.repository.HiddenRecommendationsRepository

class HideRecommendedBookUseCase(
    private val hiddenRecommendationsRepository: HiddenRecommendationsRepository,
) {
    suspend operator fun invoke(book: HiddenRecommendation): Result<Unit> = runCatchingLogged {
        hiddenRecommendationsRepository.hide(book = book)
    }
}

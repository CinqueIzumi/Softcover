package nl.rhaydus.softcover.core.lists.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.lists.domain.model.HiddenRecommendation
import nl.rhaydus.softcover.core.lists.domain.repository.HiddenRecommendationsRepository

class ObserveHiddenRecommendationsUseCase(
    private val hiddenRecommendationsRepository: HiddenRecommendationsRepository,
) {
    operator fun invoke(): Flow<List<HiddenRecommendation>> = hiddenRecommendationsRepository.hiddenRecommendations
}

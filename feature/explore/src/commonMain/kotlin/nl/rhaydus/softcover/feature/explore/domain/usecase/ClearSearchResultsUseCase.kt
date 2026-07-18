package nl.rhaydus.softcover.feature.explore.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

class ClearSearchResultsUseCase(
    private val searchRepository: ExploreRepository,
) {
    suspend operator fun invoke(): Result<Unit> = runCatchingLogged {
        searchRepository.clearSearchResults()
    }
}

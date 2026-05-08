package nl.rhaydus.softcover.feature.explore.domain.usecase

import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

class RemoveAllSearchQueriesUseCase(
    private val searchRepository: ExploreRepository,
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        searchRepository.removeAllSearchQueries()
    }
}
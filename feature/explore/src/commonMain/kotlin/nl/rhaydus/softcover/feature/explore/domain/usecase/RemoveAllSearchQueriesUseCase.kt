package nl.rhaydus.softcover.feature.explore.domain.usecase

import nl.rhaydus.softcover.core.domain.result.runCatchingLogged
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

class RemoveAllSearchQueriesUseCase(
    private val searchRepository: ExploreRepository,
) {
    suspend operator fun invoke(): Result<Unit> = runCatchingLogged {
        searchRepository.removeAllSearchQueries()
    }
}

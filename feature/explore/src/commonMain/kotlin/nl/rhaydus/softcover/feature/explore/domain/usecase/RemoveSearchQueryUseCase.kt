package nl.rhaydus.softcover.feature.explore.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

class RemoveSearchQueryUseCase(
    private val searchRepository: ExploreRepository,
) {
    suspend operator fun invoke(name: String): Result<Unit> = runCatchingLogged {
        searchRepository.removeSearchQuery(name = name)
    }
}

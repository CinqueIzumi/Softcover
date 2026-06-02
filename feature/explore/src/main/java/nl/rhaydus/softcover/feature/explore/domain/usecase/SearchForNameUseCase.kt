package nl.rhaydus.softcover.feature.explore.domain.usecase

import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

class SearchForNameUseCase(
    private val searchRepository: ExploreRepository,
    private val getUserIdUseCase: GetUserIdUseCase,
) {
    suspend operator fun invoke(name: String): Result<Unit> = runCatching {
        val userId = getUserIdUseCase().getOrThrow()

        searchRepository.searchForName(
            name = name,
            userId = userId,
        )

        searchRepository.saveSearchQuery(name = name)
    }
}

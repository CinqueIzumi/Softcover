package nl.rhaydus.softcover.feature.explore.domain.usecase

import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdUseCase

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
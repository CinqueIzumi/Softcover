package nl.rhaydus.softcover.feature.search.domain.usecase

import nl.rhaydus.softcover.feature.search.domain.repository.SearchRepository
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdUseCase

class SearchForNameUseCase(
    private val searchRepository: SearchRepository,
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
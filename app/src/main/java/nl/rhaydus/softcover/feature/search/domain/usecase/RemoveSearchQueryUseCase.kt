package nl.rhaydus.softcover.feature.search.domain.usecase

import nl.rhaydus.softcover.feature.search.domain.repository.SearchRepository

class RemoveSearchQueryUseCase(
    private val searchRepository: SearchRepository,
) {
    suspend operator fun invoke(name: String) {
        searchRepository.removeSearchQuery(name = name)
    }
}
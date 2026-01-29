package nl.rhaydus.softcover.feature.search.domain.usecase

import nl.rhaydus.softcover.feature.search.domain.repository.SearchRepository

class RemoveAllSearchQueriesUseCase(
    private val searchRepository: SearchRepository,
) {
    suspend operator fun invoke() {
        searchRepository.removeAllSearchQueries()
    }
}
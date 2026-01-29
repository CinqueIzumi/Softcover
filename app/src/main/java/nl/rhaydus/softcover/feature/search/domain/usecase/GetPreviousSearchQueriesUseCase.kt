package nl.rhaydus.softcover.feature.search.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.search.domain.repository.SearchRepository

class GetPreviousSearchQueriesUseCase(
    private val searchRepository: SearchRepository,
) {
    operator fun invoke(): Flow<List<String>> = searchRepository.previousSearchQueries
}
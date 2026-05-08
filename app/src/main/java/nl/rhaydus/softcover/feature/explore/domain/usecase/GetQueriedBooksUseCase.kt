package nl.rhaydus.softcover.feature.explore.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

class GetQueriedBooksUseCase(
    private val searchRepository: ExploreRepository
) {
    operator fun invoke(): Flow<List<Book>> = searchRepository.queriedBooks
}
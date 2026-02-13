package nl.rhaydus.softcover.feature.search.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.search.domain.repository.SearchRepository

class GetQueriedBooksUseCase(
    private val searchRepository: SearchRepository
) {
    operator fun invoke(): Flow<List<Book>> = searchRepository.queriedBooks
}
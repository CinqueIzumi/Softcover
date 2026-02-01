package nl.rhaydus.softcover.feature.caching.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.caching.domain.repository.CachingRepository

class GetAllUserBooksUseCase(
    private val cachingRepository: CachingRepository,
) {
    operator fun invoke(): Flow<List<Book>> = cachingRepository.books
}
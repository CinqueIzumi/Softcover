package nl.rhaydus.softcover.feature.reading.domain.usecase

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.caching.domain.repository.CachingRepository
import nl.rhaydus.softcover.feature.reading.domain.repository.BooksRepository

class MarkBookAsReadUseCase(
    private val repository: BooksRepository,
    private val cachingRepository: CachingRepository,
) {
    suspend operator fun invoke(book: Book): Result<Unit> = runCatching {
        val updatedBook = repository.markBookAsRead(book = book)

        cachingRepository.cacheBook(book = updatedBook)
    }
}
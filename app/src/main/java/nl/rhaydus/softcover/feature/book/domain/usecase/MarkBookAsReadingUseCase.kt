package nl.rhaydus.softcover.feature.book.domain.usecase

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.book.domain.repository.BookDetailRepository
import nl.rhaydus.softcover.feature.caching.domain.repository.CachingRepository

class MarkBookAsReadingUseCase(
    private val bookDetailRepository: BookDetailRepository,
    private val cachingRepository: CachingRepository,
) {
    suspend operator fun invoke(book: Book): Result<Unit> = runCatching {
        val updatedBook: Book = bookDetailRepository.markBookAsReading(book = book)

        cachingRepository.cacheBook(book = updatedBook)
    }
}
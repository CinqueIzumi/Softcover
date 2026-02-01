package nl.rhaydus.softcover.feature.book.domain.usecase

import nl.rhaydus.softcover.feature.book.domain.repository.BookDetailRepository
import nl.rhaydus.softcover.feature.caching.domain.repository.CachingRepository

class MarkBookAsWantToReadUseCase(
    private val bookDetailRepository: BookDetailRepository,
    private val cachingRepository: CachingRepository,
) {
    suspend operator fun invoke(bookId: Int): Result<Unit> = runCatching {
        val updatedBook = bookDetailRepository.markBookAsWantToRead(bookId = bookId)

        cachingRepository.cacheBook(book = updatedBook)
    }
}
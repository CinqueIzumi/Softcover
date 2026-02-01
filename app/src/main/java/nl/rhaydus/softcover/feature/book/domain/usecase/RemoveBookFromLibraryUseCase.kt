package nl.rhaydus.softcover.feature.book.domain.usecase

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.book.domain.repository.BookDetailRepository
import nl.rhaydus.softcover.feature.caching.domain.repository.CachingRepository

class RemoveBookFromLibraryUseCase(
    private val cachingRepository: CachingRepository,
    private val bookDetailRepository: BookDetailRepository,
) {
    suspend operator fun invoke(book: Book): Result<Unit> = runCatching {
        bookDetailRepository.removeBookFromLibrary(book = book)

        cachingRepository.removeBook(book = book)
    }
}
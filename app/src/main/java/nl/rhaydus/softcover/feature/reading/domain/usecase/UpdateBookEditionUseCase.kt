package nl.rhaydus.softcover.feature.reading.domain.usecase

import nl.rhaydus.softcover.feature.caching.domain.repository.CachingRepository
import nl.rhaydus.softcover.feature.reading.domain.repository.BooksRepository

class UpdateBookEditionUseCase(
    private val repository: BooksRepository,
    private val cachingRepository: CachingRepository,
) {
    suspend operator fun invoke(
        userBookId: Int,
        newEditionId: Int,
    ): Result<Unit> = runCatching {
        val updatedBook = repository.updateBookEdition(
            userBookId = userBookId,
            newEditionId = newEditionId,
        )

        cachingRepository.cacheBook(book = updatedBook)
    }
}
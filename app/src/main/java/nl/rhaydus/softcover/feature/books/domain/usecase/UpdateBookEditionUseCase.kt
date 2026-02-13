package nl.rhaydus.softcover.feature.books.domain.usecase

import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository

class UpdateBookEditionUseCase(
    private val repository: BooksRepository,
) {
    suspend operator fun invoke(
        userBookId: Int,
        newEditionId: Int,
    ): Result<Unit> = runCatching {
        val updatedBook = repository.updateBookEdition(
            userBookId = userBookId,
            newEditionId = newEditionId,
        )

        repository.cacheBook(book = updatedBook)
    }
}
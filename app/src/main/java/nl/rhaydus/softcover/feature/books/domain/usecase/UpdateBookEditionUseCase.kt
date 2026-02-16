package nl.rhaydus.softcover.feature.books.domain.usecase

import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository

class UpdateBookEditionUseCase(
    private val repository: BooksRepository,
) {
    suspend operator fun invoke(
        userBook: UserBook,
        newEditionId: Int,
    ): Result<Unit> = runCatching {
        val updatedBook = repository.updateBookEdition(
            userBook = userBook,
            newEditionId = newEditionId,
        )

        repository.cacheBook(book = updatedBook)
    }
}
package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.result.runCatchingLogged

class UpdateBookEditionUseCase(
    private val repository: BooksRepository,
) {
    suspend operator fun invoke(
        userBook: UserBook,
        newEditionId: Int,
    ): Result<Unit> = runCatchingLogged {
        val updatedBook = repository.updateBookEdition(
            userBook = userBook,
            newEditionId = newEditionId,
        )

        repository.cacheBook(book = updatedBook)
    }
}

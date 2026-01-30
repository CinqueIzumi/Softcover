package nl.rhaydus.softcover.feature.reading.domain.usecase

import nl.rhaydus.softcover.feature.reading.domain.repository.BooksRepository

class UpdateBookEditionUseCase(
    private val repository: BooksRepository,
) {
    suspend operator fun invoke(
        userBookId: Int,
        newEditionId: Int,
    ): Result<Unit> = runCatching {
        repository.updateBookEdition(
            userBookId = userBookId,
            newEditionId = newEditionId,
        )
    }
}
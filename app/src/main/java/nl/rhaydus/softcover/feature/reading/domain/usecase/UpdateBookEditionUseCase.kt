package nl.rhaydus.softcover.feature.reading.domain.usecase

import nl.rhaydus.softcover.feature.reading.domain.repository.BooksRepository
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdUseCase

class UpdateBookEditionUseCase(
    private val repository: BooksRepository,
    private val getUserIdUseCase: GetUserIdUseCase,
) {
    suspend operator fun invoke(
        userBookId: Int,
        newEditionId: Int,
    ): Result<Unit> = runCatching {
        val userId = getUserIdUseCase().getOrThrow()

        repository.updateBookEdition(
            userBookId = userBookId,
            newEditionId = newEditionId,
            userId = userId,
        )
    }
}
package nl.rhaydus.softcover.feature.reading.domain.usecase

import nl.rhaydus.softcover.feature.reading.domain.repository.BooksRepository
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdUseCase
import javax.inject.Inject

class UpdateBookEditionUseCase @Inject constructor(
    private val repository: BooksRepository,
    private val getUserIdUseCase: GetUserIdUseCase,
) {
    suspend operator fun invoke(
        userBookId: Int,
        newEditionId: Int,
    ) {
        getUserIdUseCase().onSuccess { userId ->
            repository.updateBookEdition(
                userBookId = userBookId,
                newEditionId = newEditionId,
                userId = userId,
            )
        }
    }
}
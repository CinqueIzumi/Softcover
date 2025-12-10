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
        // TODO: Logic for this here is duplicated, maybe add a onSuccess to the use case, which only triggers if user id != -1?
        val userId = getUserIdUseCase().getOrDefault(-1)

        if (userId == -1) return

        repository.updateBookEdition(
            userBookId = userBookId,
            newEditionId = newEditionId,
            userId = userId,
        )
    }
}
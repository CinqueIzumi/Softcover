package nl.rhaydus.softcover.feature.reading.domain.usecase

import nl.rhaydus.softcover.feature.reading.domain.repository.BooksRepository
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdUseCase
import javax.inject.Inject

class RefreshCurrentlyReadingBooksUseCase @Inject constructor(
    private val booksRepository: BooksRepository,
    private val getUserIdUseCase: GetUserIdUseCase,
) {
    suspend operator fun invoke() {
        val userId = getUserIdUseCase().getOrDefault(-1)

        if (userId == -1) {
            // TODO: Error logging or something of the sort maybe?
            return
        }

        booksRepository.refreshCurrentlyReadingBooks(userId = userId)
    }
}
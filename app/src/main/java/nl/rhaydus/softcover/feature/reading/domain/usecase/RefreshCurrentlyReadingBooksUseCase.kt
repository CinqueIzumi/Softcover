package nl.rhaydus.softcover.feature.reading.domain.usecase

import nl.rhaydus.softcover.feature.reading.domain.repository.BooksRepository
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdUseCase
import javax.inject.Inject

// TODO: All use-cases should be run catching
class RefreshCurrentlyReadingBooksUseCase @Inject constructor(
    private val booksRepository: BooksRepository,
    private val getUserIdUseCase: GetUserIdUseCase,
) {
    suspend operator fun invoke() {
        getUserIdUseCase().onSuccess { userId ->
            booksRepository.refreshCurrentlyReadingBooks(userId = userId)
        }
    }
}
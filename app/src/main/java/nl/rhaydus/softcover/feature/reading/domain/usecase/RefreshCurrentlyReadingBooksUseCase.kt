package nl.rhaydus.softcover.feature.reading.domain.usecase

import nl.rhaydus.softcover.feature.reading.domain.repository.BooksRepository
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdUseCase
import javax.inject.Inject

class RefreshCurrentlyReadingBooksUseCase @Inject constructor(
    private val booksRepository: BooksRepository,
    private val getUserIdUseCase: GetUserIdUseCase,
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        getUserIdUseCase().onSuccess { userId ->
            booksRepository.refreshCurrentlyReadingBooks(userId = userId)
        }
    }
}
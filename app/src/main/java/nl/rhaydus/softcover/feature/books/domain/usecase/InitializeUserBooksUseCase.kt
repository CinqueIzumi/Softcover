package nl.rhaydus.softcover.feature.books.domain.usecase

import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository

class InitializeUserBooksUseCase(
    private val booksRepository: BooksRepository,
    private val getUserIdUseCase: GetUserIdUseCase,
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        val userId: Int = getUserIdUseCase().getOrThrow()

        booksRepository.initializeBooks(userId = userId)
    }
}
package nl.rhaydus.softcover.feature.books.domain.usecase

import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository

class RefreshUserBooksUseCase(
    private val getUserIdUseCase: GetUserIdUseCase,
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke() = runCatching {
        val userId = getUserIdUseCase().getOrThrow()

        booksRepository.refreshUserBooks(userId = userId)
    }
}
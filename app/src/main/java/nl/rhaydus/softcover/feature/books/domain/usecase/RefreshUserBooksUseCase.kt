package nl.rhaydus.softcover.feature.books.domain.usecase

import nl.rhaydus.softcover.core.domain.model.RefreshScope
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdUseCase

class RefreshUserBooksUseCase(
    private val getUserIdUseCase: GetUserIdUseCase,
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(scope: RefreshScope = RefreshScope.All) = runCatching {
        val userId = getUserIdUseCase().getOrThrow()

        booksRepository.refreshUserBooks(userId = userId, scope = scope)
    }
}

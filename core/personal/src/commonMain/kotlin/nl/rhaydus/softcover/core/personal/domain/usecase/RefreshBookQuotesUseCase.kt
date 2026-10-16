package nl.rhaydus.softcover.core.personal.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.personal.domain.repository.QuoteRepository

class RefreshBookQuotesUseCase(
    private val repository: QuoteRepository,
) {
    suspend operator fun invoke(bookId: Int): Result<Unit> = runCatchingLogged {
        repository.refreshForBook(bookId = bookId)
    }
}

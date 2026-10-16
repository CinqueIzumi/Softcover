package nl.rhaydus.softcover.core.personal.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.personal.domain.repository.QuoteRepository

class RefreshAllQuotesUseCase(
    private val repository: QuoteRepository,
) {
    suspend operator fun invoke(): Result<Unit> = runCatchingLogged {
        repository.refreshAll()
    }
}

package nl.rhaydus.softcover.core.personal.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.personal.domain.repository.QuoteRepository

class DeleteQuoteUseCase(
    private val repository: QuoteRepository,
) {
    suspend operator fun invoke(localId: Long): Result<Unit> = runCatchingLogged {
        repository.delete(localId = localId)
    }
}

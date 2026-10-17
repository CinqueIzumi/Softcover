package nl.rhaydus.softcover.core.personal.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.Quote
import nl.rhaydus.softcover.core.personal.domain.repository.QuoteRepository

class ObserveAllQuotesUseCase(
    private val repository: QuoteRepository,
) {
    operator fun invoke(): Flow<List<Quote>> = repository.observeAll()
}

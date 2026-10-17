package nl.rhaydus.softcover.core.personal.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.Quote
import nl.rhaydus.softcover.core.personal.domain.repository.QuoteRepository

class ObserveBookQuotesUseCase(
    private val repository: QuoteRepository,
) {
    operator fun invoke(bookId: Int): Flow<List<Quote>> = repository.observeForBook(bookId = bookId)
}

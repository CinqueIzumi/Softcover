package nl.rhaydus.softcover.core.personal.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.personal.domain.model.Highlight
import nl.rhaydus.softcover.core.personal.domain.repository.HighlightRepository

class ObserveHighlightsForBookUseCase(
    private val repository: HighlightRepository,
) {
    operator fun invoke(bookId: Int): Flow<List<Highlight>> =
        repository.observeByBookId(bookId = bookId)
}

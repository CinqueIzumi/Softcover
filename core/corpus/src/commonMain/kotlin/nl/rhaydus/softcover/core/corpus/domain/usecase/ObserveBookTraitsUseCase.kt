package nl.rhaydus.softcover.core.corpus.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.corpus.domain.repository.BookTraitsRepository
import nl.rhaydus.softcover.core.domain.model.BookTraits

class ObserveBookTraitsUseCase(
    private val repository: BookTraitsRepository,
) {
    operator fun invoke(bookId: Int): Flow<BookTraits> = repository.observe(bookId = bookId)
}

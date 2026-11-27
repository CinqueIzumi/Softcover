package nl.rhaydus.softcover.core.corpus.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.corpus.domain.repository.BookTraitsRepository
import nl.rhaydus.softcover.core.domain.model.BookTraits

class ObserveAllBookTraitsUseCase(
    private val repository: BookTraitsRepository,
) {
    operator fun invoke(): Flow<Map<Int, BookTraits>> = repository.observeAll()
}

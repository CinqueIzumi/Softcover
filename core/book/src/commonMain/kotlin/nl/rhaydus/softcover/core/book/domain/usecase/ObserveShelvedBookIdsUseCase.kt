package nl.rhaydus.softcover.core.book.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.book.domain.repository.SimilarBooksRepository

class ObserveShelvedBookIdsUseCase(
    private val similarBooksRepository: SimilarBooksRepository,
) {
    operator fun invoke(ids: List<Int>): Flow<Set<Int>> = similarBooksRepository.observeShelvedBookIds(ids = ids)
}

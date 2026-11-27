package nl.rhaydus.softcover.core.corpus.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.corpus.domain.repository.BookCopyRepository
import nl.rhaydus.softcover.core.domain.model.BookCopy

class ObserveAllBookCopiesUseCase(
    private val repository: BookCopyRepository,
) {
    operator fun invoke(): Flow<Map<Int, BookCopy>> = repository.observeAll()
}

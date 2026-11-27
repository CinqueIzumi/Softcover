package nl.rhaydus.softcover.core.corpus.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.corpus.domain.repository.BookCopyRepository
import nl.rhaydus.softcover.core.domain.model.BookCopy

class ObserveBookCopyUseCase(
    private val repository: BookCopyRepository,
) {
    operator fun invoke(bookId: Int): Flow<BookCopy> = repository.observe(bookId = bookId)
}

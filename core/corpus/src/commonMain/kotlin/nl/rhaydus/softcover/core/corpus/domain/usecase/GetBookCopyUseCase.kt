package nl.rhaydus.softcover.core.corpus.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.corpus.domain.repository.BookCopyRepository
import nl.rhaydus.softcover.core.domain.model.BookCopy

class GetBookCopyUseCase(
    private val repository: BookCopyRepository,
) {
    suspend operator fun invoke(bookId: Int): Result<BookCopy> = runCatchingLogged {
        repository.get(bookId = bookId)
    }
}

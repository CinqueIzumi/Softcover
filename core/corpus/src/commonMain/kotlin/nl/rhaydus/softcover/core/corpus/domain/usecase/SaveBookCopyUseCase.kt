package nl.rhaydus.softcover.core.corpus.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.corpus.domain.repository.BookCopyRepository
import nl.rhaydus.softcover.core.domain.model.BookCopy

class SaveBookCopyUseCase(
    private val repository: BookCopyRepository,
) {
    suspend operator fun invoke(copy: BookCopy): Result<Unit> = runCatchingLogged {
        repository.save(copy = copy)
    }
}

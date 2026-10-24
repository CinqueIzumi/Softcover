package nl.rhaydus.softcover.core.corpus.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.corpus.domain.repository.BookTraitsRepository
import nl.rhaydus.softcover.core.domain.model.BookTraits

class GetBookTraitsUseCase(
    private val repository: BookTraitsRepository,
) {
    suspend operator fun invoke(bookId: Int): Result<BookTraits> = runCatchingLogged {
        repository.get(bookId = bookId)
    }
}

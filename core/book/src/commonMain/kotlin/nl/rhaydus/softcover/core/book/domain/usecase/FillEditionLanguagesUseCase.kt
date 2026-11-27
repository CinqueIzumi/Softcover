package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository

class FillEditionLanguagesUseCase(
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(editionIds: Set<Int>): Result<Unit> = runCatchingLogged {
        booksRepository.fillEditionLanguages(editionIds = editionIds)
    }
}

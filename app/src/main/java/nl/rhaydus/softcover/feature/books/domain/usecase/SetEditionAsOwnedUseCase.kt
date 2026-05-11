package nl.rhaydus.softcover.feature.books.domain.usecase

import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository

class SetEditionAsOwnedUseCase(
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(
        edition: BookEdition,
        owned: Boolean,
    ): Result<Unit> = runCatching {
        when (owned) {
            true -> booksRepository.markEditionAsOwned(edition = edition)
            false -> booksRepository.removeOwnedEdition(editionId = edition.id)
        }
    }
}

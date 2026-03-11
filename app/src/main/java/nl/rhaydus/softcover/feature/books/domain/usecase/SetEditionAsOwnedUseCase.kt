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
            true -> saveEditionAsOwned(edition = edition)
            false -> removeEditionFromOwned(edition = edition)
        }
    }

    private suspend fun removeEditionFromOwned(edition: BookEdition) {
        val listBook = booksRepository.getListBookByEditionId(editionId = edition.id)

        booksRepository.removeListBook(book = listBook)
    }

    private suspend fun saveEditionAsOwned(edition: BookEdition) {
        // TODO: I feel like this should really be done within the repository, not the use case...
        val listBook = booksRepository.markEditionAsOwned(edition = edition)

        booksRepository.cacheListBook(book = listBook)
    }
}
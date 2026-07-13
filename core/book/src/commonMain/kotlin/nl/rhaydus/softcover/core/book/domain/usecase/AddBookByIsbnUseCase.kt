package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository

/**
 * Adds a not-yet-catalogued ISBN to Hardcover via `upsert_book`, then hydrates the freshly-created
 * book through [FetchBookByIdUseCase] so it reaches the detail screen identically to a resolved
 * scan. Returns [IsbnLookupResult.Found] — the same outcome a normal resolve yields — so callers
 * navigate through a single path. The edition id falls back to the hydrated book's current edition
 * when the mutation didn't return one. Network/mapping faults propagate as a [Result] failure.
 */
class AddBookByIsbnUseCase(
    private val booksRepository: BooksRepository,
    private val fetchBookByIdUseCase: FetchBookByIdUseCase,
) {
    suspend operator fun invoke(isbn: String): Result<IsbnLookupResult.Found> = runCatchingLogged {
        val created = booksRepository.addBookByIsbn(isbn = isbn)

        val book = fetchBookByIdUseCase(id = created.bookId).getOrThrow()

        IsbnLookupResult.Found(
            book = book,
            editionId = created.editionId ?: book.currentEdition?.id,
        )
    }
}

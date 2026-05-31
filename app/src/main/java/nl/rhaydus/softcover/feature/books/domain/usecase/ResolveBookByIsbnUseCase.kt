package nl.rhaydus.softcover.feature.books.domain.usecase

import nl.rhaydus.softcover.core.domain.util.IsbnNormalizer
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository

/**
 * Resolves a scanned or manually-typed ISBN to a fully-hydrated [nl.rhaydus.softcover.core.domain.model.Book].
 *
 * The ISBN is normalized, looked up to a Hardcover `book_id`, then hydrated through the existing
 * [FetchBookByIdUseCase] so a scanned book reaches the detail screen identically to every other
 * navigation. A malformed ISBN or an ISBN with no matching edition resolves to
 * [IsbnLookupResult.UnknownEdition] on the success channel; network/mapping faults propagate as a
 * [Result] failure (the repository throws on offline, so an outage is never mistaken for "unknown").
 */
class ResolveBookByIsbnUseCase(
    private val booksRepository: BooksRepository,
    private val fetchBookByIdUseCase: FetchBookByIdUseCase,
) {
    suspend operator fun invoke(isbn: String): Result<IsbnLookupResult> = runCatching {
        val normalized = IsbnNormalizer.normalize(raw = isbn)
            ?: return@runCatching IsbnLookupResult.UnknownEdition

        val match = booksRepository.fetchEditionMatchForIsbn(isbn = normalized)
            ?: return@runCatching IsbnLookupResult.UnknownEdition

        val book = fetchBookByIdUseCase(id = match.bookId).getOrThrow()

        IsbnLookupResult.Found(book = book, editionId = match.editionId)
    }
}

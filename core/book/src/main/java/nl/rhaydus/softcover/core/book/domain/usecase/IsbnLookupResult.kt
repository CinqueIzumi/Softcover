package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.softcover.core.domain.model.Book

/**
 * Outcome of resolving an ISBN to a Hardcover book. [Found] carries the book + (when known) the
 * matched edition to preview; [UnknownEdition] is an expected business outcome (the ISBN is valid
 * but the book simply isn't catalogued yet) and carries the normalized ISBN so the caller can offer
 * to add it to Hardcover; [InvalidIsbn] is input that couldn't be normalized to a valid ISBN. None
 * of these are faults — faults surface through the enclosing [Result] failure channel.
 *
 * The variants are deliberately co-located in this sealed hierarchy (rather than split one-per-file)
 * for cohesion and the `IsbnLookupResult.Found` namespacing at call sites.
 */
sealed interface IsbnLookupResult {

    data class Found(
        val book: Book,
        val editionId: Int?,
    ) : IsbnLookupResult

    data class UnknownEdition(
        val normalizedIsbn: String,
    ) : IsbnLookupResult

    data object InvalidIsbn : IsbnLookupResult
}

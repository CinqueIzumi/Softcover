package nl.rhaydus.softcover.feature.books.domain.usecase

import nl.rhaydus.softcover.core.domain.model.Book

/**
 * Outcome of resolving an ISBN to a Hardcover book. [Found] carries the book + matched edition;
 * [UnknownEdition] is an expected business outcome (the scanned book simply isn't catalogued yet),
 * distinct from a fault — faults surface through the enclosing [Result] failure channel. Step 6.8b
 * will replace the [UnknownEdition] handling with an add-to-Hardcover flow.
 *
 * The variants are deliberately co-located in this sealed hierarchy (rather than split one-per-file)
 * for cohesion and the `IsbnLookupResult.Found` namespacing at call sites.
 */
sealed interface IsbnLookupResult {

    data class Found(
        val book: Book,
        val editionId: Int,
    ) : IsbnLookupResult

    data object UnknownEdition : IsbnLookupResult
}

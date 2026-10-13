package nl.rhaydus.softcover.feature.book_detail.presentation.state

import nl.rhaydus.softcover.core.domain.model.BookEdition

/** Keeps the editions whose ISBN-10, ISBN-13 or publisher contains [query]; a blank query keeps all. */
internal fun List<BookEdition>.filterByEditionSearch(query: String): List<BookEdition> {
    val trimmed = query.trim()

    if (trimmed.isEmpty()) return this

    return filter { edition ->
        edition.isbn10?.contains(
            other = trimmed,
            ignoreCase = true,
        ) == true ||
            edition.isbn13?.contains(
                other = trimmed,
                ignoreCase = true,
            ) == true ||
            edition.publisher?.contains(
                other = trimmed,
                ignoreCase = true,
            ) == true
    }
}

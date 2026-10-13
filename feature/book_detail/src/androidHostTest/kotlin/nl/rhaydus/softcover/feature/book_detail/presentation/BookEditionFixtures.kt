package nl.rhaydus.softcover.feature.book_detail.presentation

import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.BookEdition

internal fun buildEdition(
    id: Int,
    authors: List<Author> = emptyList(),
): BookEdition = BookEdition(
    id = id,
    canonicalId = null,
    bookId = 0,
    publisher = null,
    title = null,
    url = null,
    localImagePath = null,
    isbn10 = null,
    isbn13 = null,
    pages = null,
    audioSeconds = null,
    authors = authors,
    releaseYear = 2020,
    format = "Paperback",
    owned = false,
)

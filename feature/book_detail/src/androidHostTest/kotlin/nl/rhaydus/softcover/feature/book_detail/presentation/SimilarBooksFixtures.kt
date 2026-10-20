package nl.rhaydus.softcover.feature.book_detail.presentation

import nl.rhaydus.softcover.core.book.domain.model.SimilarBooksPage
import nl.rhaydus.softcover.core.domain.model.Book

internal fun buildSimilarBook(
    id: Int,
    canonicalId: Int? = null,
): Book = Book(
    id = id,
    canonicalId = canonicalId,
    title = "Book $id",
    editions = emptyList(),
    defaultEdition = null,
    rating = 0.0,
    description = "",
    releaseYear = 2020,
    coverUrl = "",
    authors = emptyList(),
    usersCount = 0,
    ratingsCount = 0,
    bookSeries = null,
    positionsInSeries = emptyList(),
    isCompilation = false,
    userBook = null,
)

internal fun buildSimilarBooksPage(
    ids: List<Int>,
    nextOffset: Int,
    hasMore: Boolean,
): SimilarBooksPage = SimilarBooksPage(
    books = ids.map { buildSimilarBook(id = it) },
    nextOffset = nextOffset,
    hasMore = hasMore,
)

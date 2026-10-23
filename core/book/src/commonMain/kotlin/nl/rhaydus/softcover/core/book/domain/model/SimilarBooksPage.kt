package nl.rhaydus.softcover.core.book.domain.model

import nl.rhaydus.softcover.core.domain.model.Book

/**
 * One page of similar books.
 *
 * [books] can be empty while [hasMore] is true when every candidate in the window was filtered out.
 */
data class SimilarBooksPage(
    val books: List<Book>,
    val nextOffset: Int,
    val hasMore: Boolean,
)

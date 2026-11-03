package nl.rhaydus.softcover.core.profile.domain.model

import nl.rhaydus.softcover.core.domain.model.Book

/** The longest book finished in range; [pages] is the reader's edition count, falling back to the book's. */
data class LongestBook(
    val book: Book,
    val pages: Int,
)

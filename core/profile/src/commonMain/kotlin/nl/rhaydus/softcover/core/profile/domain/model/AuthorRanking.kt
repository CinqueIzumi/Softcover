package nl.rhaydus.softcover.core.profile.domain.model

import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book

/**
 * @property averageRating `null` when none of the author's books in the range is rated.
 * @property leadBook The author's most recently finished book in the range.
 */
data class AuthorRanking(
    val author: Author,
    val booksRead: Int,
    val averageRating: Double?,
    val leadBook: Book,
)

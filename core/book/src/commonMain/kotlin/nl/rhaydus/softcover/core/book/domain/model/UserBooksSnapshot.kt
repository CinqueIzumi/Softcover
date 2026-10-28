package nl.rhaydus.softcover.core.book.domain.model

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.UserTag

/**
 * One library fetch: the books plus the signed-in user's own tags on them, keyed by each book's final
 * id (the canonical id when the book was merged).
 */
data class UserBooksSnapshot(
    val books: List<Book>,
    val userTagsByBookId: Map<Int, List<UserTag>>,
)

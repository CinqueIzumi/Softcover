package nl.rhaydus.softcover.core.lists.domain.model

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.SystemVibe

/**
 * @property source The vibe [books] came from; Top Picks only when the reader's Recommendations leave nothing.
 * @property isLoading True only until the first refresh settles while nothing is cached yet.
 */
data class RecommendedFeed(
    val books: List<Book>,
    val isLoading: Boolean,
    val source: SystemVibe,
)

package nl.rhaydus.softcover.core.lists.domain.model

import nl.rhaydus.softcover.core.domain.model.Book

/** @property isLoading True only until the first refresh settles while nothing is cached yet. */
data class RecommendedFeed(
    val books: List<Book>,
    val isLoading: Boolean,
)

package nl.rhaydus.softcover.feature.reading.presentation.collector

import nl.rhaydus.softcover.core.domain.model.Book

/** The state slices every Reading cover surface's UI model is derived from. */
internal data class CoverModelsSnapshot(
    val featuredBook: Book?,
    val books: List<Book>,
    val pickUpNextBooks: List<Book>,
    val trendingBook: Book?,
    val verdictBook: Book?,
)

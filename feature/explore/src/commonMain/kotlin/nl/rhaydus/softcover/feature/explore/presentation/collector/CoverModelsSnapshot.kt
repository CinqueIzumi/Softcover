package nl.rhaydus.softcover.feature.explore.presentation.collector

import nl.rhaydus.softcover.core.domain.model.Book

/** The state fields the explore screen's cover UI models are derived from. */
internal data class CoverModelsSnapshot(
    val featuredUpcomingRelease: Book?,
    val trendingBooks: List<Book>,
    val becauseYouReadBooks: List<Book>,
    val continueSeriesBooks: List<Book>,
    val queriedBooks: List<Book>,
)

package nl.rhaydus.softcover.feature.explore.presentation.collector

import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.domain.model.Book

/** The state fields the explore rails' and search results' book card models are derived from. */
internal data class BookCardModelsSnapshot(
    val trendingBooks: List<Book>,
    val becauseYouReadBooks: List<Book>,
    val continueSeriesBooks: List<Book>,
    val queriedBooks: List<Book>,
    val unreleasedCoverOverlays: Map<Int, CoverOverlayUiModel>,
    val loadingTrendingBooks: Boolean,
    val loadingBecauseYouReadBooks: Boolean,
    val loadingContinueSeriesBooks: Boolean,
    val isLoading: Boolean,
)

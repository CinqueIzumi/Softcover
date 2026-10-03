package nl.rhaydus.softcover.feature.explore.presentation.collector

import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.domain.model.Book

/** The state fields the explore rails' and search results' book card models are derived from. */
internal data class BookCardModelsSnapshot(
    val trendingBooks: List<Book>,
    val becauseYouReadBooks: List<Book>,
    val continueSeriesBooks: List<Book>,
    val queriedBooks: List<Book>,
    val trendingCovers: Map<Int, CoverUiModel>,
    val becauseYouReadCovers: Map<Int, CoverUiModel>,
    val continueSeriesCovers: Map<Int, CoverUiModel>,
    val queriedBookCovers: Map<Int, CoverUiModel>,
    val unreleasedCoverOverlays: Map<Int, CoverOverlayUiModel>,
)

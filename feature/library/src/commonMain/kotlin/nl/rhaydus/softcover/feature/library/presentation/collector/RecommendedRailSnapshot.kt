package nl.rhaydus.softcover.feature.library.presentation.collector

import nl.rhaydus.softcover.core.domain.model.Book

/** @property wantToReadCount `null` while the Want to read shelf is not a visible tab or has not loaded. */
internal data class RecommendedRailSnapshot(
    val wantToReadCount: Int?,
    val recommendedBooks: List<Book>,
    val loadingRecommendedBooks: Boolean,
    val selectionMode: Boolean,
    val isRearranging: Boolean,
)

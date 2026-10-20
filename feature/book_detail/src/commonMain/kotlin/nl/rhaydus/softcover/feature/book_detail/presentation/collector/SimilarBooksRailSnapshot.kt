package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.collections.immutable.ImmutableList

internal data class SimilarBooksRailSnapshot(
    val sourceBookId: Int,
    val loadedIds: ImmutableList<Int>,
    val isLoading: Boolean,
    val hasMore: Boolean,
)

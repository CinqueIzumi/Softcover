package nl.rhaydus.softcover.feature.book_detail.presentation.collector

internal data class SimilarBooksLoadSnapshot(
    val sourceBookId: Int?,
    val isTheBookLens: Boolean,
)

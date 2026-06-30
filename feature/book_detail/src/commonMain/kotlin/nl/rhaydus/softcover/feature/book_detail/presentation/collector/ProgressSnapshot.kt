package nl.rhaydus.softcover.feature.book_detail.presentation.collector

internal data class ProgressSnapshot(
    val bookId: Int?,
    val totalPages: Int?,
    val currentPage: Int,
    val totalSeconds: Int?,
    val currentSeconds: Int,
)

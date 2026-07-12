package nl.rhaydus.softcover.feature.explore.domain.model

/**
 * A book the user hid from the "up next in your series" suggestions.
 *
 * [seriesId] and [seriesPosition] are what let the series cursor advance past this book: without
 * them the next-in-series query keeps returning the hidden book, and the series stops suggesting
 * anything at all. They are null only on rows dismissed before schema v45, which
 * [nl.rhaydus.softcover.feature.explore.domain.usecase.EnrichDismissedContinueSeriesMetadataUseCase]
 * backfills.
 */
data class DismissedSeriesBook(
    val bookId: Int,
    val title: String?,
    val coverUrl: String?,
    val authorText: String?,
    val seriesName: String?,
    val seriesId: Int?,
    val seriesPosition: Double?,
)

package nl.rhaydus.softcover.feature.explore.domain.model

data class DismissedSeriesBook(
    val bookId: Int,
    val title: String?,
    val coverUrl: String?,
    val authorText: String?,
    val seriesName: String?,
)

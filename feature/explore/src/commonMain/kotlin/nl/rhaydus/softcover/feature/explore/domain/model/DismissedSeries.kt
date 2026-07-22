package nl.rhaydus.softcover.feature.explore.domain.model

data class DismissedSeries(
    val seriesId: Int,
    val seriesName: String?,
    val coverUrl: String?,
    val authorText: String?,
    val bookCount: Int?,
)

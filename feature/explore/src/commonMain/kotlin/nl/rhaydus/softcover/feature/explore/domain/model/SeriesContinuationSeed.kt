package nl.rhaydus.softcover.feature.explore.domain.model

// Public, like its DismissedSeries/DismissedSeriesBook siblings: it crosses the public
// ExploreRepository boundary now that next-in-series lookups are batched per seed rather than
// issued one at a time from primitives.
data class SeriesContinuationSeed(
    val seriesId: Int,
    val afterPosition: Double,
)

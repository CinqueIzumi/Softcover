package nl.rhaydus.softcover.core.profile.domain.model

data class ReadingLife(
    val booksByYear: List<YearCount>,
    val pagesByYear: List<YearCount>,
    val pagesByMonth: List<MonthCount>,
    val genres: List<GenreSlice>,
    val ratings: RatingsDistribution,
    val recentlyLoved: List<LovedBook>,
    val trackedYears: Int = 0,
)

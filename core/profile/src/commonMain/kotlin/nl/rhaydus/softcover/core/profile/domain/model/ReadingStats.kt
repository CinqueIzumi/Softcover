package nl.rhaydus.softcover.core.profile.domain.model

/** The aggregates of the books finished inside one stats range. */
data class ReadingStats(
    val booksRead: Int = 0,
    val totalPages: Int = 0,
    val genres: GenreBreakdown = GenreBreakdown(),
    val ratings: RatingsDistribution = RatingsDistribution(),
    val authorDemographics: AuthorDemographics = AuthorDemographics(),
    val topAuthors: List<AuthorRanking> = emptyList(),
    val formatSplit: FormatSplit = FormatSplit(),
)

package nl.rhaydus.softcover.core.profile.domain.model

import kotlinx.datetime.LocalDate

data class UserProfileData(
    val profileImageUrl: String,
    val name: String,
    val username: String,
    val bio: String,
    val booksRead: Int,
    val totalPagesRead: Int,
    val averageRating: Double,
    val readingStreak: Int,
    val recentReadingDays: Set<LocalDate> = emptySet(),
    val booksByYear: List<YearCount> = emptyList(),
    val pagesByYear: List<YearCount> = emptyList(),
    val pagesByMonth: List<MonthCount> = emptyList(),
    val genres: List<GenreSlice> = emptyList(),
    val ratings: RatingsDistribution = RatingsDistribution(),
    val recentlyLoved: List<LovedBook> = emptyList(),
    val trackedYears: Int = 0,
    val authorDemographics: AuthorDemographics = AuthorDemographics(),
)

package nl.rhaydus.softcover.core.profile.data.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData

@Serializable
internal data class UserProfileDataEntity(
    val profileImageUrl: String,
    val name: String,
    val username: String = "",
    val bio: String,
    val booksRead: Int,
    val totalPagesRead: Int,
    val averageRating: Double,
    val readingStreak: Int,
    val recentReadingDays: List<String> = emptyList(),
    val booksByYear: List<YearCountEntity> = emptyList(),
    val pagesByYear: List<YearCountEntity> = emptyList(),
    val pagesByMonth: List<MonthCountEntity> = emptyList(),
    val genres: List<GenreSliceEntity> = emptyList(),
    val ratings: RatingsDistributionEntity = RatingsDistributionEntity(),
    val recentlyLoved: List<LovedBookEntity> = emptyList(),
    val trackedYears: Int = 0,
    val authorDemographics: AuthorDemographicsEntity = AuthorDemographicsEntity(),
)

internal fun UserProfileDataEntity.toModel(): UserProfileData = UserProfileData(
    profileImageUrl = profileImageUrl,
    name = name,
    username = username,
    bio = bio,
    booksRead = booksRead,
    totalPagesRead = totalPagesRead,
    averageRating = averageRating,
    readingStreak = readingStreak,
    recentReadingDays = recentReadingDays
        .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
        .toSet(),
    booksByYear = booksByYear.map { it.toModel() },
    pagesByYear = pagesByYear.map { it.toModel() },
    pagesByMonth = pagesByMonth.map { it.toModel() },
    genres = genres.map { it.toModel() },
    ratings = ratings.toModel(),
    recentlyLoved = recentlyLoved.map { it.toModel() },
    trackedYears = trackedYears,
    authorDemographics = authorDemographics.toModel(),
)

internal fun UserProfileData.toEntity(): UserProfileDataEntity = UserProfileDataEntity(
    profileImageUrl = profileImageUrl,
    name = name,
    username = username,
    bio = bio,
    booksRead = booksRead,
    totalPagesRead = totalPagesRead,
    averageRating = averageRating,
    readingStreak = readingStreak,
    recentReadingDays = recentReadingDays
        .sorted()
        .map { it.toString() },
    booksByYear = booksByYear.map { it.toEntity() },
    pagesByYear = pagesByYear.map { it.toEntity() },
    pagesByMonth = pagesByMonth.map { it.toEntity() },
    genres = genres.map { it.toEntity() },
    ratings = ratings.toEntity(),
    recentlyLoved = recentlyLoved.map { it.toEntity() },
    trackedYears = trackedYears,
    authorDemographics = authorDemographics.toEntity(),
)

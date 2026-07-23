package nl.rhaydus.softcover.core.profile.data.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
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
    // Flat alongside `genres` rather than nested into a GenreBreakdownEntity: every field added to
    // this entity since its first version carries a default so a cache written by an older build
    // still decodes, and re-shaping `genres` from an array into an object would break exactly that.
    // The domain pairs the two into GenreBreakdown; only the stored form is flat.
    val genreTaggedBookCount: Int = 0,
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
    genres = decodeGenreBreakdown(),
    ratings = ratings.toModel(),
    recentlyLoved = recentlyLoved.map { it.toModel() },
    trackedYears = trackedYears,
    authorDemographics = authorDemographics.toModel(),
)

// Slices with no denominator are discarded rather than shown. That pairing only happens on a cache
// written before 3.1.1, whose fractions are shares of genre *assignments* - a basis the current
// section would render as if it were a share of books, drawing a confidently wrong bar (a whole-Fantasy
// reader's ~17% instead of 100%) against a full 100% track. The refresh replaces it within the
// session either way; until then the honest state is the same "fills in" placeholder a new reader
// sees, not a number computed on a basis this build no longer uses.
private fun UserProfileDataEntity.decodeGenreBreakdown(): GenreBreakdown {
    if (genreTaggedBookCount <= 0) return GenreBreakdown()

    return GenreBreakdown(
        slices = genres.map { it.toModel() },
        taggedBookCount = genreTaggedBookCount,
    )
}

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
    genres = genres.slices.map { it.toEntity() },
    genreTaggedBookCount = genres.taggedBookCount,
    ratings = ratings.toEntity(),
    recentlyLoved = recentlyLoved.map { it.toEntity() },
    trackedYears = trackedYears,
    authorDemographics = authorDemographics.toEntity(),
)

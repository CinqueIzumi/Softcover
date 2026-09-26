package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import nl.rhaydus.softcover.core.domain.model.Gender
import nl.rhaydus.softcover.core.profile.domain.model.AuthorDemographics
import nl.rhaydus.softcover.core.profile.domain.model.DemographicBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenderSlice
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenreSlice
import nl.rhaydus.softcover.core.profile.domain.model.LovedBook
import nl.rhaydus.softcover.core.profile.domain.model.MonthCount
import nl.rhaydus.softcover.core.profile.domain.model.RatingBand
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution
import nl.rhaydus.softcover.core.profile.domain.model.ReadingLife
import nl.rhaydus.softcover.core.profile.domain.model.YearCount

/**
 * A representative [ReadingLife] sample (the redline spec's sample reader, Elena Marchetti) shared by
 * both the mobile and desktop `@StandardPreview`s so the two stay in sync. Split into one builder per
 * field (below) purely to keep this assembly function short.
 */
internal fun profileReadingLifePreview(): ReadingLife = ReadingLife(
    booksByYear = previewBooksByYear(),
    pagesByYear = previewPagesByYear(),
    pagesByMonth = previewPagesByMonth(),
    genres = previewGenres(),
    ratings = previewRatings(),
    recentlyLoved = previewRecentlyLoved(),
    trackedYears = 8,
    authorDemographics = previewAuthorDemographics(),
)

private fun previewBooksByYear(): List<YearCount> = listOf(
    YearCount(
        year = 2018,
        count = 18,
    ),
    YearCount(
        year = 2019,
        count = 22,
    ),
    YearCount(
        year = 2020,
        count = 31,
    ),
    YearCount(
        year = 2021,
        count = 26,
    ),
    YearCount(
        year = 2022,
        count = 24,
    ),
    YearCount(
        year = 2023,
        count = 29,
    ),
    YearCount(
        year = 2024,
        count = 33,
    ),
    YearCount(
        year = 2025,
        count = 31,
    ),
)

private fun previewPagesByYear(): List<YearCount> = listOf(
    YearCount(
        year = 2018,
        count = 5_040,
    ),
    YearCount(
        year = 2019,
        count = 6_160,
    ),
    YearCount(
        year = 2020,
        count = 8_680,
    ),
    YearCount(
        year = 2021,
        count = 7_280,
    ),
    YearCount(
        year = 2022,
        count = 6_720,
    ),
    YearCount(
        year = 2023,
        count = 8_120,
    ),
    YearCount(
        year = 2024,
        count = 9_240,
    ),
    YearCount(
        year = 2025,
        count = 8_680,
    ),
)

private fun previewPagesByMonth(): List<MonthCount> =
    listOf(3_400, 2_100, 4_200, 3_900, 5_100, 4_800, 3_300, 2_900, 4_600, 5_300, 4_100, 3_800)
        .mapIndexed { index, count ->
            MonthCount(
                year = 2026,
                month = index + 1,
                count = count,
            )
        }

// Each fraction is its slice's count over the 148 genre-tagged books, so the sample reader's bars sum
// past 100 - which is the point: overlapping genres are what the ranked-bar shape exists to show.
private fun previewGenres(): GenreBreakdown = GenreBreakdown(
    slices = listOf(
        GenreSlice(
            name = "Literary fiction",
            count = 73,
            fraction = 0.493,
        ),
        GenreSlice(
            name = "Fantasy",
            count = 51,
            fraction = 0.345,
        ),
        GenreSlice(
            name = "History",
            count = 32,
            fraction = 0.216,
        ),
        GenreSlice(
            name = "Mystery",
            count = 28,
            fraction = 0.189,
        ),
        GenreSlice(
            name = "Science fiction",
            count = 19,
            fraction = 0.128,
        ),
    ),
    taggedBookCount = 148,
)

private fun previewAuthorDemographics(): AuthorDemographics = AuthorDemographics(
    genderSlices = listOf(
        GenderSlice(
            gender = Gender.Female,
            count = 12,
            fraction = 0.40,
        ),
        GenderSlice(
            gender = Gender.Male,
            count = 9,
            fraction = 0.30,
        ),
        GenderSlice(
            gender = Gender.Other,
            count = 1,
            fraction = 0.033,
        ),
        GenderSlice(
            gender = Gender.Unknown,
            count = 8,
            fraction = 0.267,
        ),
    ),
    knownGenderCount = 22,
    unknownGenderCount = 8,
    bipocBreakdown = DemographicBreakdown(
        yesCount = 2,
        noCount = 5,
        unknownCount = 23,
    ),
    lgbtqBreakdown = DemographicBreakdown(
        yesCount = 1,
        noCount = 1,
        unknownCount = 28,
    ),
)

private fun previewRatings(): RatingsDistribution = RatingsDistribution(
    average = 4.2,
    totalRatings = 214,
    halfStarBuckets = listOf(2, 1, 3, 5, 12, 22, 54, 58, 57),
    band = RatingBand.GENEROUS,
)

private fun previewRecentlyLoved(): List<LovedBook> = listOf(
    LovedBook(
        coverUrl = "",
        title = "The Bee Sting",
        author = "Paul Murray",
        ratingStars = 4.5,
        monthLabel = "Jun 2026",
    ),
    LovedBook(
        coverUrl = "",
        title = "Cloud Cuckoo Land",
        author = "Anthony Doerr",
        ratingStars = 4.0,
        monthLabel = "May 2026",
    ),
    LovedBook(
        coverUrl = "",
        title = "Sea of Tranquility",
        author = "Emily St John Mandel",
        ratingStars = 4.5,
        monthLabel = "Apr 2026",
    ),
)

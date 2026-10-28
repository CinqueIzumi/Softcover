package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import nl.rhaydus.softcover.core.domain.model.Gender
import nl.rhaydus.softcover.core.domain.model.ReadingFormat
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.core.profile.domain.model.AuthorDemographics
import nl.rhaydus.softcover.core.profile.domain.model.AuthorRanking
import nl.rhaydus.softcover.core.profile.domain.model.DemographicBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.FormatSplit
import nl.rhaydus.softcover.core.profile.domain.model.GenderSlice
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenreSlice
import nl.rhaydus.softcover.core.profile.domain.model.HourlyPages
import nl.rhaydus.softcover.core.profile.domain.model.LengthBand
import nl.rhaydus.softcover.core.profile.domain.model.MonthCount
import nl.rhaydus.softcover.core.profile.domain.model.MonthlyPages
import nl.rhaydus.softcover.core.profile.domain.model.RatingBand
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution
import nl.rhaydus.softcover.core.profile.domain.model.ReadingActivity
import nl.rhaydus.softcover.core.profile.domain.model.ReadingHistory
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStats
import nl.rhaydus.softcover.core.profile.domain.model.RecentlyLovedBook
import nl.rhaydus.softcover.core.profile.domain.model.TimeToFinish
import nl.rhaydus.softcover.core.profile.domain.model.YearCount

/**
 * A representative [ReadingStats] sample (the redline spec's sample reader, Elena Marchetti) shared by
 * both the mobile and desktop `@StandardPreview`s so the two stay in sync. Split into one builder per
 * field (below) purely to keep this assembly function short.
 */
internal fun profileReadingStatsPreview(): ReadingStats = ReadingStats(
    booksRead = 214,
    totalPages = 128_406,
    genres = previewGenres(),
    ratings = previewRatings(),
    authorDemographics = previewAuthorDemographics(),
    topAuthors = previewTopAuthors(),
    formatSplit = previewFormatSplit(),
)

internal fun profileTimeToFinishPreview(): TimeToFinish = TimeToFinish(
    readCount = 31,
    medianDays = 9,
    bandMedianDays = mapOf(
        LengthBand.UNDER_250 to 5,
        LengthBand.FROM_250_TO_450 to 9,
        LengthBand.OVER_450 to 23,
    ),
    excludedReadCount = 2,
)

private fun previewFormatSplit(): FormatSplit = FormatSplit(
    counts = mapOf(
        ReadingFormat.Physical to 111,
        ReadingFormat.Ebook to 66,
        ReadingFormat.Audio to 37,
    ),
)

private fun previewTopAuthors(): List<AuthorRanking> = listOf(
    previewRanking(
        id = 1,
        name = "Ursula K. Le Guin",
        leadTitle = "The Left Hand of Darkness",
        booksRead = 4,
        averageRating = 4.6,
    ),
    previewRanking(
        id = 2,
        name = "Terry Pratchett",
        leadTitle = "Guards! Guards!",
        booksRead = 3,
        averageRating = 4.3,
    ),
    previewRanking(
        id = 3,
        name = "Becky Chambers",
        leadTitle = "A Psalm for the Wild-Built",
        booksRead = 3,
        averageRating = 4.1,
    ),
    previewRanking(
        id = 4,
        name = "Susanna Clarke",
        leadTitle = "Piranesi",
        booksRead = 2,
        averageRating = 4.8,
    ),
    previewRanking(
        id = 5,
        name = "Kazuo Ishiguro",
        leadTitle = "Klara and the Sun",
        booksRead = 2,
        averageRating = 3.9,
    ),
)

private fun previewRanking(
    id: Int,
    name: String,
    leadTitle: String,
    booksRead: Int,
    averageRating: Double,
): AuthorRanking = AuthorRanking(
    author = PreviewData.baseAuthor.copy(
        id = id,
        name = name,
    ),
    booksRead = booksRead,
    averageRating = averageRating,
    leadBook = PreviewData.baseBook.copy(
        id = id,
        title = leadTitle,
    ),
)

/** The pages-read-per-month half of the same sample reader, summed across every year. */
internal fun profileMonthlyPagesPreview(): MonthlyPages = MonthlyPages.AcrossYears(
    pages = listOf(9_200, 7_800, 14_800, 11_400, 8_600, 6_900, 12_100, 13_500, 10_300, 12_700, 9_800, 11_300),
)

/** The pages-read-per-hour half of the same sample reader, mostly in the evening. */
internal fun profileHourlyPagesPreview(): HourlyPages = HourlyPages(
    pages = listOf(
        2_400, 700, 100, 0, 0, 200,
        900, 1_300, 1_100, 1_700, 1_900, 2_300,
        2_000, 1_800, 1_400, 2_100, 2_800, 4_600,
        6_900, 9_800, 14_200, 17_500, 12_100, 6_300,
    ),
)

/** The pages-read-per-day half of the same sample reader: a handful of days a week through the first ten months. */
internal fun profileReadingActivityPreview(): ReadingActivity = ReadingActivity(
    pagesByDay = (0 until 300)
        .filter { it % 7 != 0 && it % 5 != 0 }
        .associate { dayOfYear ->
            LocalDate(
                year = 2025,
                month = 1,
                day = 1,
            ).plus(
                value = dayOfYear,
                unit = DateTimeUnit.DAY,
            ) to 12 + (dayOfYear * 37) % 140
        },
    streaks = emptyList(),
)

/** The [ReadingHistory] half of the same sample reader. */
internal fun profileReadingHistoryPreview(): ReadingHistory = ReadingHistory(
    booksByYear = previewBooksByYear(),
    pagesByYear = previewPagesByYear(),
    yearsRead = (2025 downTo 2018).toList(),
    trackedYears = 8,
    pagesByMonth = previewPagesByMonth(),
    recentlyLoved = previewRecentlyLoved(),
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

private fun previewRecentlyLoved(): List<RecentlyLovedBook> = listOf(
    RecentlyLovedBook(
        bookId = 1,
        ratingStars = 4.5,
        finishedOn = LocalDate(
            2026,
            6,
            14,
        ),
    ),
    RecentlyLovedBook(
        bookId = 2,
        ratingStars = 4.0,
        finishedOn = LocalDate(
            2026,
            5,
            9,
        ),
    ),
    RecentlyLovedBook(
        bookId = 3,
        ratingStars = 4.5,
        finishedOn = LocalDate(
            2026,
            4,
            21,
        ),
    ),
)

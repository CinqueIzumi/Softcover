package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import nl.rhaydus.common.formatDecimalNumber
import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.softcover.core.component.share.ReadingLifeGenre
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenreSlice
import nl.rhaydus.softcover.core.profile.domain.model.MonthCount
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution
import nl.rhaydus.softcover.core.profile.domain.model.ReadingLife
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadingLifeShareCardMapperTest {
    private fun life(
        pagesByMonth: List<MonthCount> = emptyList(),
        slices: List<GenreSlice> = emptyList(),
        average: Double = 0.0,
        trackedYears: Int = 0,
    ): ReadingLife = ReadingLife(
        booksByYear = emptyList(),
        pagesByYear = emptyList(),
        pagesByMonth = pagesByMonth,
        genres = GenreBreakdown(slices = slices),
        ratings = RatingsDistribution(average = average),
        recentlyLoved = emptyList(),
        trackedYears = trackedYears,
    )

    private fun profile(
        name: String = "Ada Lovelace",
        imageUrl: String = "https://avatar",
        booksRead: Int = 12,
        totalPagesRead: Int = 950,
        streak: Int = 4,
    ): UserProfileData = UserProfileData(
        profileImageUrl = imageUrl,
        name = name,
        username = "ada",
        bio = "",
        booksRead = booksRead,
        totalPagesRead = totalPagesRead,
        averageRating = 0.0,
        readingStreak = streak,
    )

    private fun slice(
        name: String,
        fraction: Double,
    ): GenreSlice = GenreSlice(
        name = name,
        count = 1,
        fraction = fraction,
    )

    @Nested
    inner class ToReadingLifeShareCardUiModel {
        @Test
        fun `carries the reader identity and totals from the profile`() {
            // ----- Arrange -----
            val profile = profile(
                name = "Ada Lovelace",
                booksRead = 12,
                totalPagesRead = 950,
                streak = 4,
            )

            // ----- Act -----
            val result = life(trackedYears = 3).toReadingLifeShareCardUiModel(profile)

            // ----- Assert -----
            result.readerName shouldBe "Ada Lovelace"
            result.totalPagesRead shouldBe formatGroupedNumber(950)
            result.dayStreak shouldBe "4"
            result.trackedYears shouldBe "3"
        }

        @Test
        fun `uses the singular book for a single book read`() {
            // ----- Arrange -----
            val profile = profile(booksRead = 1)

            // ----- Act -----
            val result = life().toReadingLifeShareCardUiModel(profile)

            // ----- Assert -----
            result.totalBooksLine shouldEndWith "1 book"
        }

        @Test
        fun `uses the plural books for several books read`() {
            // ----- Arrange -----
            val profile = profile(booksRead = 12)

            // ----- Act -----
            val result = life().toReadingLifeShareCardUiModel(profile)

            // ----- Assert -----
            result.totalBooksLine shouldEndWith "12 books"
        }

        @Test
        fun `passes the avatar url through when present`() {
            // ----- Arrange -----
            val profile = profile(imageUrl = "https://avatar")

            // ----- Act -----
            val result = life().toReadingLifeShareCardUiModel(profile)

            // ----- Assert -----
            result.avatarUrl shouldBe "https://avatar"
        }

        @Test
        fun `maps a blank avatar url to null`() {
            // ----- Arrange -----
            val profile = profile(imageUrl = "   ")

            // ----- Act -----
            val result = life().toReadingLifeShareCardUiModel(profile)

            // ----- Assert -----
            result.avatarUrl.shouldBeNull()
        }

        @Test
        fun `keeps the rating numeric and formats it to one decimal`() {
            // ----- Arrange -----
            val life = life(average = 4.5)

            // ----- Act -----
            val result = life.toReadingLifeShareCardUiModel(profile())

            // ----- Assert -----
            result.averageRating shouldBe 4.5
            result.averageRatingText shouldBe formatDecimalNumber(
                value = 4.5,
                fractionDigits = 1,
            )
        }

        @Test
        fun `keeps only the first three genres in their given order`() {
            // ----- Arrange -----
            val life = life(
                slices = listOf(
                    slice(
                        name = "Fantasy",
                        fraction = 0.5,
                    ),
                    slice(
                        name = "Horror",
                        fraction = 0.4,
                    ),
                    slice(
                        name = "Romance",
                        fraction = 0.3,
                    ),
                    slice(
                        name = "Poetry",
                        fraction = 0.1,
                    ),
                ),
            )

            // ----- Act -----
            val result = life.toReadingLifeShareCardUiModel(profile())

            // ----- Assert -----
            result.topGenres.map { it.name } shouldBe listOf("Fantasy", "Horror", "Romance")
        }

        @Test
        fun `rounds genre fractions to whole-number percent labels`() {
            // ----- Arrange -----
            val life = life(
                slices = listOf(
                    slice(
                        name = "Fantasy",
                        fraction = 1.0,
                    ),
                    slice(
                        name = "Horror",
                        fraction = 0.666,
                    ),
                    slice(
                        name = "Romance",
                        fraction = 0.0,
                    ),
                ),
            )

            // ----- Act -----
            val result = life.toReadingLifeShareCardUiModel(profile())

            // ----- Assert -----
            result.topGenres shouldBe listOf(
                ReadingLifeGenre(
                    name = "Fantasy",
                    percentLabel = "100%",
                ),
                ReadingLifeGenre(
                    name = "Horror",
                    percentLabel = "67%",
                ),
                ReadingLifeGenre(
                    name = "Romance",
                    percentLabel = "0%",
                ),
            )
        }

        @Test
        fun `has no genres when there are no slices`() {
            // ----- Arrange -----
            val life = life(slices = emptyList())

            // ----- Act -----
            val result = life.toReadingLifeShareCardUiModel(profile())

            // ----- Assert -----
            result.topGenres shouldHaveSize 0
        }
    }

    @Nested
    inner class Initials {
        private fun initialsFor(name: String): String = life()
            .toReadingLifeShareCardUiModel(profile(name = name))
            .readerInitials

        @Test
        fun `uses the first letters of the first and last word`() {
            // ----- Arrange -----
            val name = "Ada Augusta King"

            // ----- Act -----
            val result = initialsFor(name)

            // ----- Assert -----
            result shouldBe "AK"
        }

        @Test
        fun `uppercases lowercase letters`() {
            // ----- Arrange -----
            val name = "ada lovelace"

            // ----- Act -----
            val result = initialsFor(name)

            // ----- Assert -----
            result shouldBe "AL"
        }

        @Test
        fun `gives a single letter for a single word name`() {
            // ----- Arrange -----
            val name = "Cher"

            // ----- Act -----
            val result = initialsFor(name)

            // ----- Assert -----
            result shouldBe "C"
        }

        @Test
        fun `ignores surrounding whitespace`() {
            // ----- Arrange -----
            val name = "  Ada Lovelace  "

            // ----- Act -----
            val result = initialsFor(name)

            // ----- Assert -----
            result shouldBe "AL"
        }

        @Test
        fun `skips empty words produced by repeated spaces`() {
            // ----- Arrange -----
            val name = "Ada   Lovelace"

            // ----- Act -----
            val result = initialsFor(name)

            // ----- Assert -----
            result shouldBe "AL"
        }

        @Test
        fun `falls back to a question mark for a blank name`() {
            // ----- Arrange -----
            val name = "   "

            // ----- Act -----
            val result = initialsFor(name)

            // ----- Assert -----
            result shouldBe "?"
        }
    }

    @Nested
    inner class CurrentYearMonthlyPages {
        private fun monthsFor(pagesByMonth: List<MonthCount>): List<Int> = life(pagesByMonth = pagesByMonth)
            .toReadingLifeShareCardUiModel(profile())
            .pagesByMonth

        @Test
        fun `gives twelve zeros when there is no month data`() {
            // ----- Arrange -----
            val data = emptyList<MonthCount>()

            // ----- Act -----
            val result = monthsFor(data)

            // ----- Assert -----
            result shouldBe List(12) { 0 }
        }

        @Test
        fun `places counts at their month and zero fills the gaps`() {
            // ----- Arrange -----
            val data = listOf(
                MonthCount(
                    year = 2025,
                    month = 1,
                    count = 100,
                ),
                MonthCount(
                    year = 2025,
                    month = 12,
                    count = 300,
                ),
            )

            // ----- Act -----
            val result = monthsFor(data)

            // ----- Assert -----
            result shouldBe listOf(100, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 300)
        }

        @Test
        fun `uses only the latest year present`() {
            // ----- Arrange -----
            val data = listOf(
                MonthCount(
                    year = 2024,
                    month = 3,
                    count = 999,
                ),
                MonthCount(
                    year = 2025,
                    month = 2,
                    count = 40,
                ),
            )

            // ----- Act -----
            val result = monthsFor(data)

            // ----- Assert -----
            result shouldBe listOf(0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
        }
    }
}

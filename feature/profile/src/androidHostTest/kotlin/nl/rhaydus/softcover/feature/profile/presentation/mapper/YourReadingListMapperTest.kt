package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.types.shouldBeInstanceOf
import nl.rhaydus.softcover.core.component.chart.ChartTone
import nl.rhaydus.softcover.core.component.chip.ChipTrailing
import nl.rhaydus.softcover.core.component.statistic.StatTileVariant
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.profile.domain.model.DidNotFinishStats
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenreSlice
import nl.rhaydus.softcover.core.profile.domain.model.ReadingListStats
import nl.rhaydus.softcover.core.profile.domain.model.StopQuarter
import nl.rhaydus.softcover.feature.profile.presentation.state.YourReadingListUiModel
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class YourReadingListMapperTest {
    private fun buildStats(
        bookCount: Int,
        unknown: Int = 0,
        first: Int = 0,
        second: Int = 0,
        third: Int = 0,
        last: Int = 0,
        genres: List<GenreSlice> = emptyList(),
    ): DidNotFinishStats = DidNotFinishStats(
        bookCount = bookCount,
        quarterCounts = mapOf(
            StopQuarter.FIRST to first,
            StopQuarter.SECOND to second,
            StopQuarter.THIRD to third,
            StopQuarter.LAST to last,
        ),
        unknownStopCount = unknown,
        genres = GenreBreakdown(slices = genres),
    )

    private fun slice(
        name: String,
        count: Int,
    ): GenreSlice = GenreSlice(
        name = name,
        count = count,
        fraction = 0.5,
    )

    private fun setAsideModel(didNotFinish: DidNotFinishStats?): YourReadingListUiModel = yourReadingListUiModel(
        stats = ReadingListStats(),
        booksRead = 0,
        didNotFinish = didNotFinish,
        scope = StatsScope.AllTime,
    )

    private fun readingListModel(
        stats: ReadingListStats? = ReadingListStats(),
        booksRead: Int? = 0,
        didNotFinish: DidNotFinishStats? = DidNotFinishStats(),
        scope: StatsScope = StatsScope.AllTime,
    ): YourReadingListUiModel = yourReadingListUiModel(
        stats = stats,
        booksRead = booksRead,
        didNotFinish = didNotFinish,
        scope = scope,
    )

    private fun YourReadingListUiModel.framing(): String =
        header.description.shouldNotBeNull().joinToString(separator = "") { it.text }

    private fun YourReadingListUiModel.waitUnit(): String =
        timeOnList.variant.shouldBeInstanceOf<StatTileVariant.Compact>().unit

    private fun YourReadingListUiModel.waitCaption(): String? =
        timeOnList.variant.shouldBeInstanceOf<StatTileVariant.Compact>().caption

    private fun YourReadingListUiModel.descriptionText(): String =
        setAside.description.shouldNotBeNull().joinToString(separator = "") { it.text }

    @Nested
    inner class SetAside {
        @Test
        fun `null stats yields the loading chapter with no genres`() {
            // ----- Arrange -----
            val stats: DidNotFinishStats? = null

            // ----- Act -----
            val result = setAsideModel(stats)

            // ----- Assert -----
            result.setAside.isLoading shouldBe true
            result.didNotFinish.shouldNotBeNull().stopPoint.shouldNotBeNull().isLoading shouldBe true
            result.didNotFinish.shouldNotBeNull().genres.shouldBeNull()
        }

        @Test
        fun `no book put down yields the empty header and no section`() {
            // ----- Arrange -----
            val stats = DidNotFinishStats()

            val counted = setAsideModel(
                buildStats(
                    bookCount = 1,
                    unknown = 1,
                ),
            )

            // ----- Act -----
            val result = setAsideModel(stats)

            // ----- Assert -----
            result.didNotFinish.shouldBeNull()
            result.setAside.isLoading shouldBe false
            result.setAside.headlineText shouldNotBe counted.setAside.headlineText
            result.setAside.description shouldNotBe counted.setAside.description
        }

        @Test
        fun `one book put down yields the section`() {
            // ----- Arrange -----
            val stats = buildStats(
                bookCount = 1,
                unknown = 1,
            )

            // ----- Act -----
            val result = setAsideModel(stats)

            // ----- Assert -----
            result.didNotFinish.shouldNotBeNull()
        }

        @Test
        fun `uses the singular headline for a single book`() {
            // ----- Arrange -----
            val stats = buildStats(
                bookCount = 1,
                unknown = 1,
            )

            // ----- Act -----
            val result = setAsideModel(stats)

            // ----- Assert -----
            result.setAside.headlineText shouldBe "1 book you didn't finish"
        }

        @Test
        fun `uses the plural headline for several books`() {
            // ----- Arrange -----
            val stats = buildStats(
                bookCount = 3,
                unknown = 3,
            )

            // ----- Act -----
            val result = setAsideModel(stats)

            // ----- Assert -----
            result.setAside.headlineText shouldBe "3 books you didn't finish"
        }

        @Test
        fun `names the quarter holding more than half of the known stop points`() {
            // ----- Arrange -----
            val stats = buildStats(
                bookCount = 5,
                first = 1,
                last = 3,
                second = 1,
            )

            // ----- Act -----
            val result = setAsideModel(stats)

            // ----- Assert -----
            result.descriptionText() shouldContain "Most of these you stopped in the last quarter."
        }

        @Test
        fun `counts only the known stop points toward the majority`() {
            // ----- Arrange -----
            val stats = buildStats(
                bookCount = 6,
                unknown = 2,
                third = 3,
                first = 1,
            )

            // ----- Act -----
            val result = setAsideModel(stats)

            // ----- Assert -----
            result.descriptionText() shouldContain "Most of these you stopped in the third quarter."
        }

        @Test
        fun `omits the sentence when a quarter holds exactly half`() {
            // ----- Arrange -----
            val stats = buildStats(
                bookCount = 4,
                first = 2,
                second = 1,
                third = 1,
            )

            // ----- Act -----
            val result = setAsideModel(stats)

            // ----- Assert -----
            result.descriptionText() shouldNotContain "Most of these"
        }

        @Test
        fun `omits the sentence with fewer than two known stop points`() {
            // ----- Arrange -----
            val stats = buildStats(
                bookCount = 3,
                unknown = 2,
                first = 1,
            )

            // ----- Act -----
            val result = setAsideModel(stats)

            // ----- Assert -----
            result.descriptionText() shouldNotContain "Most of these"
        }

        @Test
        fun `is omitted when no stop point is known`() {
            // ----- Arrange -----
            val stats = buildStats(
                bookCount = 2,
                unknown = 2,
            )

            // ----- Act -----
            val result = setAsideModel(stats)

            // ----- Assert -----
            result.didNotFinish.shouldNotBeNull().stopPoint.shouldBeNull()
        }

        @Test
        fun `always lists the four columns in book order`() {
            // ----- Arrange -----
            val stats = buildStats(
                bookCount = 3,
                last = 2,
                first = 1,
            )

            // ----- Act -----
            val result = setAsideModel(stats)

            // ----- Assert -----
            val columns = result.didNotFinish.shouldNotBeNull().stopPoint.shouldNotBeNull().columns
            columns.map { it.label } shouldBe listOf("First", "Second", "Third", "Last")
        }

        @Test
        fun `shares the known stops as whole percentages and shows an empty quarter as 0 percent`() {
            // ----- Arrange -----
            val stats = buildStats(
                bookCount = 5,
                unknown = 1,
                first = 1,
                third = 3,
            )

            // ----- Act -----
            val result = setAsideModel(stats)

            // ----- Assert -----
            val columns = result.didNotFinish.shouldNotBeNull().stopPoint.shouldNotBeNull().columns
            columns.map { it.value } shouldBe listOf("25%", "0%", "75%", "0%")
            columns.map { it.fraction } shouldBe listOf(0.25f, 0f, 0.75f, 0f)
        }

        @Test
        fun `leads with the tallest column`() {
            // ----- Arrange -----
            val stats = buildStats(
                bookCount = 6,
                first = 1,
                second = 2,
                last = 3,
            )

            // ----- Act -----
            val result = setAsideModel(stats)

            // ----- Assert -----
            val columns = result.didNotFinish.shouldNotBeNull().stopPoint.shouldNotBeNull().columns
            columns.map { it.tone == ChartTone.Lead } shouldBe listOf(false, false, false, true)
        }

        @Test
        fun `leads with the earlier quarter on a tie`() {
            // ----- Arrange -----
            val stats = buildStats(
                bookCount = 6,
                first = 1,
                second = 2,
                third = 2,
                last = 1,
            )

            // ----- Act -----
            val result = setAsideModel(stats)

            // ----- Assert -----
            val columns = result.didNotFinish.shouldNotBeNull().stopPoint.shouldNotBeNull().columns
            columns.map { it.tone == ChartTone.Lead } shouldBe listOf(false, true, false, false)
        }

        @Test
        fun `reads each column with its quarter, book count and share`() {
            // ----- Arrange -----
            val stats = buildStats(
                bookCount = 9,
                first = 5,
                third = 1,
                last = 3,
            )

            // ----- Act -----
            val result = setAsideModel(stats)

            // ----- Assert -----
            val columns = result.didNotFinish.shouldNotBeNull().stopPoint.shouldNotBeNull().columns

            columns.map { it.contentDescription } shouldBe listOf(
                "First quarter, 5 books, 56%",
                "Second quarter, no books, 0%",
                "Third quarter, 1 book, 11%",
                "Last quarter, 3 books, 33%",
            )
        }

        @Test
        fun `reads a column with the same share its value shows`() {
            // ----- Arrange -----
            val stats = buildStats(
                bookCount = 5,
                unknown = 1,
                first = 1,
                third = 3,
            )

            // ----- Act -----
            val result = setAsideModel(stats)

            // ----- Assert -----
            val columns = result.didNotFinish.shouldNotBeNull().stopPoint.shouldNotBeNull().columns
            columns.map { it.contentDescription.substringAfterLast(", ") } shouldBe columns.map { it.value }
        }

        @Test
        fun `has no unknown caption when every stop is known`() {
            // ----- Arrange -----
            val stats = buildStats(
                bookCount = 1,
                first = 1,
            )

            // ----- Act -----
            val result = setAsideModel(stats)

            // ----- Assert -----
            result.didNotFinish.shouldNotBeNull().unknownStopCaption.shouldBeNull()
        }

        @Test
        fun `captions one book without a stop point in the singular`() {
            // ----- Arrange -----
            val stats = buildStats(
                bookCount = 2,
                unknown = 1,
                first = 1,
            )

            // ----- Act -----
            val result = setAsideModel(stats)

            // ----- Assert -----
            result.didNotFinish.shouldNotBeNull().unknownStopCaption shouldBe "1 book has no stop point."
        }

        @Test
        fun `captions several books without a stop point in the plural`() {
            // ----- Arrange -----
            val stats = buildStats(
                bookCount = 4,
                unknown = 3,
                first = 1,
            )

            // ----- Act -----
            val result = setAsideModel(stats)

            // ----- Assert -----
            result.didNotFinish.shouldNotBeNull().unknownStopCaption shouldBe "3 books have no stop point."
        }

        @Test
        fun `is omitted when no book carries a genre`() {
            // ----- Arrange -----
            val stats = buildStats(
                bookCount = 2,
                unknown = 2,
            )

            // ----- Act -----
            val result = setAsideModel(stats)

            // ----- Assert -----
            result.didNotFinish.shouldNotBeNull().genres.shouldBeNull()
        }

        @Test
        fun `shows the five most common genres with the count trailing`() {
            // ----- Arrange -----
            val stats = buildStats(
                bookCount = 9,
                unknown = 9,
                genres = listOf(
                    slice(
                        "Fantasy",
                        7,
                    ),
                    slice(
                        "Mystery",
                        5,
                    ),
                    slice(
                        "Poetry",
                        4,
                    ),
                    slice(
                        "History",
                        3,
                    ),
                    slice(
                        "Horror",
                        2,
                    ),
                    slice(
                        "Romance",
                        1,
                    ),
                ),
            )

            // ----- Act -----
            val result = setAsideModel(stats)

            // ----- Assert -----
            val chips = result.didNotFinish.shouldNotBeNull().genres.shouldNotBeNull().chips
            chips.map { it.label } shouldBe listOf("Fantasy", "Mystery", "Poetry", "History", "Horror")
            chips.map { it.trailing } shouldBe listOf(7, 5, 4, 3, 2).map { ChipTrailing.Count(text = it.toString()) }
        }

        @Test
        fun `speaks the genre with its book count`() {
            // ----- Arrange -----
            val stats = buildStats(
                bookCount = 4,
                unknown = 4,
                genres = listOf(
                    slice(
                        "Fantasy",
                        3,
                    ),
                    slice(
                        "Horror",
                        1,
                    ),
                ),
            )

            // ----- Act -----
            val result = setAsideModel(stats)

            // ----- Assert -----
            val chips = result.didNotFinish.shouldNotBeNull().genres.shouldNotBeNull().chips
            chips.map { it.contentDescription } shouldBe listOf("Fantasy, 3 books", "Horror, 1 book")
        }
    }

    @Nested
    inner class Header {
        @Test
        fun `says what was added and read in the scope`() {
            // ----- Arrange -----
            val stats = ReadingListStats(addedCount = 12)

            // ----- Act -----
            val result = readingListModel(
                stats = stats,
                booksRead = 5,
                scope = StatsScope.CalendarYear(2025),
            )

            // ----- Assert -----
            result.framing() shouldBe "You added 12 books to your list in 2025 and read 5."
        }

        @Test
        fun `says what was added and read across all time without a scope phrase`() {
            // ----- Arrange -----
            val stats = ReadingListStats(addedCount = 12)

            // ----- Act -----
            val result = readingListModel(
                stats = stats,
                booksRead = 5,
            )

            // ----- Assert -----
            result.framing() shouldBe "You added 12 books to your list and read 5."
        }

        @Test
        fun `says none were added when books were read in the scope`() {
            // ----- Arrange -----
            val stats = ReadingListStats(addedCount = 0)

            // ----- Act -----
            val result = readingListModel(
                stats = stats,
                booksRead = 5,
                scope = StatsScope.CalendarYear(2025),
            )

            // ----- Assert -----
            result.framing() shouldBe "You read 5 books in 2025 and added none to your list."
        }

        @Test
        fun `says none were added when books were read across all time`() {
            // ----- Arrange -----
            val stats = ReadingListStats(addedCount = 0)

            // ----- Act -----
            val result = readingListModel(
                stats = stats,
                booksRead = 1,
            )

            // ----- Assert -----
            result.framing() shouldBe "You read 1 book and added none to your list."
        }

        @Test
        fun `leaves the read count out when none were read in the scope`() {
            // ----- Arrange -----
            val stats = ReadingListStats(addedCount = 12)

            // ----- Act -----
            val result = readingListModel(
                stats = stats,
                booksRead = 0,
                scope = StatsScope.CalendarYear(2025),
            )

            // ----- Assert -----
            result.framing() shouldBe "You added 12 books to your list in 2025."
        }

        @Test
        fun `leaves the read count out when none were read across all time`() {
            // ----- Arrange -----
            val stats = ReadingListStats(addedCount = 1)

            // ----- Act -----
            val result = readingListModel(
                stats = stats,
                booksRead = 0,
            )

            // ----- Assert -----
            result.framing() shouldBe "You added 1 book to your list."
        }

        @Test
        fun `says what would show here when nothing was added or read in the scope`() {
            // ----- Arrange -----
            val stats = ReadingListStats(addedCount = 0)

            // ----- Act -----
            val result = readingListModel(
                stats = stats,
                booksRead = 0,
                scope = StatsScope.CalendarYear(2025),
            )

            // ----- Assert -----
            result.framing() shouldBe "Books you add to your list and finish in 2025 show up here."
        }

        @Test
        fun `says what would show here when nothing was added or read across all time`() {
            // ----- Arrange -----
            val stats = ReadingListStats(addedCount = 0)

            // ----- Act -----
            val result = readingListModel(
                stats = stats,
                booksRead = 0,
            )

            // ----- Assert -----
            result.framing() shouldBe "Books you add to your list and finish show up here."
        }
    }

    @Nested
    inner class TimeOnList {
        private fun waitFor(days: Int): YourReadingListUiModel = readingListModel(
            stats = ReadingListStats(
                addedCount = 10,
                waitDays = days,
                waitedCount = 4,
            ),
        )

        @Test
        fun `counts 13 days in days`() {
            // ----- Arrange -----
            val days = 13

            // ----- Act -----
            val result = waitFor(days)

            // ----- Assert -----
            result.timeOnList.stat.shouldNotBeNull().value shouldBe 13.0
            result.waitUnit() shouldBe "days"
        }

        @Test
        fun `switches to weeks at 14 days`() {
            // ----- Arrange -----
            val days = 14

            // ----- Act -----
            val result = waitFor(days)

            // ----- Assert -----
            result.timeOnList.stat.shouldNotBeNull().value shouldBe 2.0
            result.waitUnit() shouldBe "weeks"
        }

        @Test
        fun `still counts 55 days in weeks`() {
            // ----- Arrange -----
            val days = 55

            // ----- Act -----
            val result = waitFor(days)

            // ----- Assert -----
            result.timeOnList.stat.shouldNotBeNull().value shouldBe 8.0
            result.waitUnit() shouldBe "weeks"
        }

        @Test
        fun `switches to months at 56 days`() {
            // ----- Arrange -----
            val days = 56

            // ----- Act -----
            val result = waitFor(days)

            // ----- Assert -----
            result.timeOnList.stat.shouldNotBeNull().value shouldBe 2.0
            result.waitUnit() shouldBe "months"
        }

        @Test
        fun `still counts 729 days in months`() {
            // ----- Arrange -----
            val days = 729

            // ----- Act -----
            val result = waitFor(days)

            // ----- Assert -----
            result.timeOnList.stat.shouldNotBeNull().value shouldBe 24.0
            result.waitUnit() shouldBe "months"
        }

        @Test
        fun `switches to years at 730 days`() {
            // ----- Arrange -----
            val days = 730

            // ----- Act -----
            val result = waitFor(days)

            // ----- Assert -----
            result.timeOnList.stat.shouldNotBeNull().value shouldBe 2.0
            result.waitUnit() shouldBe "years"
        }

        @Test
        fun `uses the singular unit for a single day`() {
            // ----- Arrange -----
            val days = 1

            // ----- Act -----
            val result = waitFor(days)

            // ----- Assert -----
            result.timeOnList.stat.shouldNotBeNull().value shouldBe 1.0
            result.waitUnit() shouldBe "day"
        }

        @Test
        fun `names the single book that waited`() {
            // ----- Arrange -----
            val stats = ReadingListStats(
                addedCount = 3,
                waitDays = 20,
                waitedCount = 1,
            )

            // ----- Act -----
            val result = readingListModel(stats = stats)

            // ----- Assert -----
            result.waitCaption() shouldBe "Typical wait from adding a book to starting it, for the one book you started."
        }

        @Test
        fun `counts the books that waited when there are several`() {
            // ----- Arrange -----
            val stats = ReadingListStats(
                addedCount = 40,
                waitDays = 20,
                waitedCount = 27,
            )

            // ----- Act -----
            val result = readingListModel(stats = stats)

            // ----- Assert -----
            result.waitCaption() shouldBe "Typical wait from adding a book to starting it, for the 27 books you started."
        }

        @Test
        fun `has no number and a scoped caption when no listed book was started in the scope`() {
            // ----- Arrange -----
            val stats = ReadingListStats(
                addedCount = 3,
                waitDays = null,
            )

            // ----- Act -----
            val result = readingListModel(
                stats = stats,
                scope = StatsScope.CalendarYear(2025),
            )

            // ----- Assert -----
            result.timeOnList.stat.shouldBeNull()
            result.waitCaption() shouldBe "No book from your list was started in 2025."
        }

        @Test
        fun `has no number and an invitation caption when no listed book was ever started`() {
            // ----- Arrange -----
            val stats = ReadingListStats(
                addedCount = 3,
                waitDays = null,
            )

            // ----- Act -----
            val result = readingListModel(stats = stats)

            // ----- Assert -----
            result.timeOnList.stat.shouldBeNull()
            result.waitCaption() shouldBe "Start a book from your list and the wait shows up here."
        }
    }

    @Nested
    inner class StillToComeLoading {
        @Test
        fun `loads while the reading list stats are missing`() {
            // ----- Arrange -----
            val stats: ReadingListStats? = null

            // ----- Act -----
            val result = readingListModel(
                stats = stats,
                booksRead = 5,
            )

            // ----- Assert -----
            result.header.isLoading shouldBe true
            result.added.stat.isLoading shouldBe true
            result.read.stat.isLoading shouldBe true
            result.timeOnList.stat.shouldNotBeNull().isLoading shouldBe true
        }

        @Test
        fun `loads while the books read are missing`() {
            // ----- Arrange -----
            val booksRead: Int? = null

            // ----- Act -----
            val result = readingListModel(
                stats = ReadingListStats(addedCount = 4),
                booksRead = booksRead,
            )

            // ----- Assert -----
            result.header.isLoading shouldBe true
            result.added.stat.isLoading shouldBe true
            result.read.stat.isLoading shouldBe true
            result.timeOnList.stat.shouldNotBeNull().isLoading shouldBe true
        }

        @Test
        fun `settles once both are present`() {
            // ----- Arrange -----
            val stats = ReadingListStats(
                addedCount = 4,
                waitDays = 20,
                waitedCount = 2,
            )

            // ----- Act -----
            val result = readingListModel(
                stats = stats,
                booksRead = 5,
            )

            // ----- Assert -----
            result.header.isLoading shouldBe false
            result.added.stat.isLoading shouldBe false
            result.read.stat.isLoading shouldBe false
            result.timeOnList.stat.shouldNotBeNull().isLoading shouldBe false
        }

        @Test
        fun `shows the set aside books while the rest still loads`() {
            // ----- Arrange -----
            val didNotFinish = buildStats(
                bookCount = 3,
                unknown = 3,
            )

            // ----- Act -----
            val result = readingListModel(
                stats = null,
                booksRead = null,
                didNotFinish = didNotFinish,
            )

            // ----- Assert -----
            result.header.isLoading shouldBe true
            result.setAside.isLoading shouldBe false
            result.setAside.headlineText shouldBe "3 books you didn't finish"
            result.didNotFinish.shouldNotBeNull()
        }
    }
}

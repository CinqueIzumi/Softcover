package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import nl.rhaydus.softcover.core.component.chart.ChartTone
import nl.rhaydus.softcover.core.component.chip.ChipTrailing
import nl.rhaydus.softcover.core.profile.domain.model.DidNotFinishStats
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenreSlice
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

    private fun YourReadingListUiModel.descriptionText(): String =
        header.description.shouldNotBeNull().joinToString(separator = "") { it.text }

    @Nested
    inner class YourReadingListUiModelMapping {
        @Test
        fun `null stats yields the loading chapter with no genres`() {
            // ----- Arrange -----
            val stats: DidNotFinishStats? = null

            // ----- Act -----
            val result = yourReadingListUiModel(stats)

            // ----- Assert -----
            result.isLoading shouldBe true
            result.header.isLoading shouldBe true
            result.didNotFinish.shouldNotBeNull().stopPoint.shouldNotBeNull().isLoading shouldBe true
            result.didNotFinish.shouldNotBeNull().genres.shouldBeNull()
        }

        @Test
        fun `no book put down yields the empty header and no section`() {
            // ----- Arrange -----
            val stats = DidNotFinishStats()

            val counted = yourReadingListUiModel(
                buildStats(
                    bookCount = 1,
                    unknown = 1,
                ),
            )

            // ----- Act -----
            val result = yourReadingListUiModel(stats)

            // ----- Assert -----
            result.didNotFinish.shouldBeNull()
            result.isLoading shouldBe false
            result.header.isLoading shouldBe false
            result.header.headlineText shouldNotBe counted.header.headlineText
            result.header.description shouldNotBe counted.header.description
        }

        @Test
        fun `one book put down yields the section`() {
            // ----- Arrange -----
            val stats = buildStats(
                bookCount = 1,
                unknown = 1,
            )

            // ----- Act -----
            val result = yourReadingListUiModel(stats)

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
            val result = yourReadingListUiModel(stats)

            // ----- Assert -----
            result.header.headlineText shouldBe "1 book you didn't finish"
        }

        @Test
        fun `uses the plural headline for several books`() {
            // ----- Arrange -----
            val stats = buildStats(
                bookCount = 3,
                unknown = 3,
            )

            // ----- Act -----
            val result = yourReadingListUiModel(stats)

            // ----- Assert -----
            result.header.headlineText shouldBe "3 books you didn't finish"
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
            val result = yourReadingListUiModel(stats)

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
            val result = yourReadingListUiModel(stats)

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
            val result = yourReadingListUiModel(stats)

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
            val result = yourReadingListUiModel(stats)

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
            val result = yourReadingListUiModel(stats)

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
            val result = yourReadingListUiModel(stats)

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
            val result = yourReadingListUiModel(stats)

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
            val result = yourReadingListUiModel(stats)

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
            val result = yourReadingListUiModel(stats)

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
            val result = yourReadingListUiModel(stats)

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
            val result = yourReadingListUiModel(stats)

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
            val result = yourReadingListUiModel(stats)

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
            val result = yourReadingListUiModel(stats)

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
            val result = yourReadingListUiModel(stats)

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
            val result = yourReadingListUiModel(stats)

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
            val result = yourReadingListUiModel(stats)

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
            val result = yourReadingListUiModel(stats)

            // ----- Assert -----
            val chips = result.didNotFinish.shouldNotBeNull().genres.shouldNotBeNull().chips
            chips.map { it.contentDescription } shouldBe listOf("Fantasy, 3 books", "Horror, 1 book")
        }
    }
}

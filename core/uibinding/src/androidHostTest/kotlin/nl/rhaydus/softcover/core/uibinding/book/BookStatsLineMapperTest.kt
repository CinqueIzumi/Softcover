package nl.rhaydus.softcover.core.uibinding.book

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import nl.rhaydus.softcover.core.component.bookcard.BookCardLinePart
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookStatsLineMapperTest {
    @Nested
    inner class Year {
        @Test
        fun `a release year becomes a text part`() {
            // ----- Arrange -----
            val releaseYear: Int? = 1965
            val usersCount: Int? = null
            val rating: Double? = null

            // ----- Act -----
            val line = bookStatsLine(
                releaseYear = releaseYear,
                usersCount = usersCount,
                rating = rating,
            )

            // ----- Assert -----
            line.shouldNotBeNull().parts shouldBe listOf(BookCardLinePart.Text(text = "1965"))
        }

        @Test
        fun `a null or -1 year is omitted`() {
            // ----- Arrange -----
            val years = listOf(null, -1)

            // ----- Act -----
            val lines = years.map { year ->
                bookStatsLine(
                    releaseYear = year,
                    usersCount = 5,
                    rating = null,
                )
            }

            // ----- Assert -----
            lines.forEach { line ->
                line.shouldNotBeNull().parts shouldBe listOf(BookCardLinePart.Text(text = "5 readers"))
            }
        }
    }

    @Nested
    inner class Readers {
        @Test
        fun `a reader count becomes a readers text part`() {
            // ----- Arrange -----
            val releaseYear: Int? = null
            val usersCount: Int? = 12
            val rating: Double? = null

            // ----- Act -----
            val line = bookStatsLine(
                releaseYear = releaseYear,
                usersCount = usersCount,
                rating = rating,
            )

            // ----- Assert -----
            line.shouldNotBeNull().parts shouldBe listOf(BookCardLinePart.Text(text = "12 readers"))
        }

        @Test
        fun `a zero reader count is kept`() {
            // ----- Arrange -----
            val releaseYear: Int? = null
            val usersCount: Int? = 0
            val rating: Double? = null

            // ----- Act -----
            val line = bookStatsLine(
                releaseYear = releaseYear,
                usersCount = usersCount,
                rating = rating,
            )

            // ----- Assert -----
            line.shouldNotBeNull().parts shouldBe listOf(BookCardLinePart.Text(text = "0 readers"))
        }

        @Test
        fun `a large count is grouped`() {
            // ----- Arrange -----
            val releaseYear: Int? = null
            val usersCount: Int? = 1_234_567
            val rating: Double? = null

            // ----- Act -----
            val line = bookStatsLine(
                releaseYear = releaseYear,
                usersCount = usersCount,
                rating = rating,
            )

            // ----- Assert -----
            val text = line.shouldNotBeNull().parts.single().shouldBeInstanceOf<BookCardLinePart.Text>().text
            text.endsWith(" readers") shouldBe true
            text.filter { it.isDigit() } shouldBe "1234567 readers".filter { it.isDigit() }
            (text.length > "1234567 readers".length) shouldBe true
        }
    }

    @Nested
    inner class Rating {
        @Test
        fun `a rating shows one decimal`() {
            // ----- Arrange -----
            val releaseYear: Int? = null
            val usersCount: Int? = null
            val rating: Double? = 4.26

            // ----- Act -----
            val line = bookStatsLine(
                releaseYear = releaseYear,
                usersCount = usersCount,
                rating = rating,
            )

            // ----- Assert -----
            line.shouldNotBeNull().parts shouldBe listOf(BookCardLinePart.Rating(
                label = "4.3",
                contentDescription = "Rated 4.3 out of 5",
            ),)
        }

        @Test
        fun `a null or 0 rating is omitted`() {
            // ----- Arrange -----
            val ratings = listOf(null, 0.0)

            // ----- Act -----
            val lines = ratings.map { rating ->
                bookStatsLine(
                    releaseYear = 2000,
                    usersCount = null,
                    rating = rating,
                )
            }

            // ----- Assert -----
            lines.forEach { line ->
                line.shouldNotBeNull().parts shouldBe listOf(BookCardLinePart.Text(text = "2000"))
            }
        }
    }

    @Nested
    inner class Composition {
        @Test
        fun `parts keep the year, readers, rating order`() {
            // ----- Arrange -----
            val releaseYear: Int? = 1965
            val usersCount: Int? = 12
            val rating: Double? = 4.2

            // ----- Act -----
            val line = bookStatsLine(
                releaseYear = releaseYear,
                usersCount = usersCount,
                rating = rating,
            )

            // ----- Assert -----
            val parts = line.shouldNotBeNull().parts
            parts shouldHaveSize 3
            parts[0] shouldBe BookCardLinePart.Text(text = "1965")
            parts[1] shouldBe BookCardLinePart.Text(text = "12 readers")
            parts[2] shouldBe BookCardLinePart.Rating(
                label = "4.2",
                contentDescription = "Rated 4.2 out of 5",
            )
        }

        @Test
        fun `no parts yields no line`() {
            // ----- Arrange -----
            val inputs = listOf(
                Triple<Int?, Int?, Double?>(
                    null,
                    null,
                    null,
                ),
                Triple<Int?, Int?, Double?>(
                    -1,
                    null,
                    0.0,
                ),
            )

            // ----- Act -----
            val lines = inputs.map { (year, readers, rating) ->
                bookStatsLine(
                    releaseYear = year,
                    usersCount = readers,
                    rating = rating,
                )
            }

            // ----- Assert -----
            lines.forEach { line -> line.shouldBeNull() }
        }
    }
}

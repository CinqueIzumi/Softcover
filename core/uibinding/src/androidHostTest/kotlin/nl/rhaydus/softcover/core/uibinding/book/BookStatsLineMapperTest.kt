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
            // ----- Act -----
            val line = bookStatsLine(
                releaseYear = 1965,
                usersCount = null,
                rating = null,
            )

            // ----- Assert -----
            line.shouldNotBeNull().parts shouldBe listOf(BookCardLinePart.Text(text = "1965"))
        }

        @Test
        fun `a null or -1 year is omitted`() {
            listOf(null, -1).forEach { year ->
                // ----- Act -----
                val line = bookStatsLine(
                    releaseYear = year,
                    usersCount = 5,
                    rating = null,
                )

                // ----- Assert -----
                line.shouldNotBeNull().parts shouldBe listOf(BookCardLinePart.Text(text = "5 readers"))
            }
        }
    }

    @Nested
    inner class Readers {
        @Test
        fun `a reader count becomes a readers text part`() {
            // ----- Act -----
            val line = bookStatsLine(
                releaseYear = null,
                usersCount = 12,
                rating = null,
            )

            // ----- Assert -----
            line.shouldNotBeNull().parts shouldBe listOf(BookCardLinePart.Text(text = "12 readers"))
        }

        @Test
        fun `a zero reader count is kept`() {
            // ----- Act -----
            val line = bookStatsLine(
                releaseYear = null,
                usersCount = 0,
                rating = null,
            )

            // ----- Assert -----
            line.shouldNotBeNull().parts shouldBe listOf(BookCardLinePart.Text(text = "0 readers"))
        }

        @Test
        fun `a large count is grouped`() {
            // ----- Act -----
            val line = bookStatsLine(
                releaseYear = null,
                usersCount = 1_234_567,
                rating = null,
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
            // ----- Act -----
            val line = bookStatsLine(
                releaseYear = null,
                usersCount = null,
                rating = 4.26,
            )

            // ----- Assert -----
            line.shouldNotBeNull().parts shouldBe listOf(BookCardLinePart.Rating(label = "4.3"))
        }

        @Test
        fun `a null or 0 rating is omitted`() {
            listOf(null, 0.0).forEach { rating ->
                // ----- Act -----
                val line = bookStatsLine(
                    releaseYear = 2000,
                    usersCount = null,
                    rating = rating,
                )

                // ----- Assert -----
                line.shouldNotBeNull().parts shouldBe listOf(BookCardLinePart.Text(text = "2000"))
            }
        }
    }

    @Nested
    inner class Composition {
        @Test
        fun `parts keep the year, readers, rating order`() {
            // ----- Act -----
            val line = bookStatsLine(
                releaseYear = 1965,
                usersCount = 12,
                rating = 4.2,
            )

            // ----- Assert -----
            val parts = line.shouldNotBeNull().parts
            parts shouldHaveSize 3
            parts[0] shouldBe BookCardLinePart.Text(text = "1965")
            parts[1] shouldBe BookCardLinePart.Text(text = "12 readers")
            parts[2] shouldBe BookCardLinePart.Rating(label = "4.2")
        }

        @Test
        fun `no parts yields no line`() {
            listOf(
                Triple(
                    null,
                    null,
                    null,
                ),
                Triple(
                    -1,
                    null,
                    0.0,
                ),
            ).forEach { (year, readers, rating) ->
                // ----- Act -----
                val line = bookStatsLine(
                    releaseYear = year,
                    usersCount = readers,
                    rating = rating,
                )

                // ----- Assert -----
                line.shouldBeNull()
            }
        }
    }
}

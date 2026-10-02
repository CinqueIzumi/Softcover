package nl.rhaydus.softcover.core.presentation.model

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookInitialCoverTest {
    private fun stubBook(
        title: String = "Dune",
        rating: Double = 4.5,
        seriesText: String?,
    ): Book = mockk {
        every {
            currentEdition
        } returns null
        every {
            defaultEdition
        } returns null
        every {
            coverUrl
        } returns "https://example.com/cover.jpg"
        every {
            userBook
        } returns null
        every {
            userBookRead
        } returns null
        every {
            this@mockk.title
        } returns title
        every {
            this@mockk.seriesText
        } returns seriesText
        every {
            this@mockk.rating
        } returns rating
    }

    @Nested
    inner class FromBook {
        @Test
        fun `seeds a blank series text when the book has no series`() {
            // ----- Arrange -----
            val book = stubBook(seriesText = null)

            // ----- Act -----
            val result = BookInitialCover.fromBook(book = book)

            // ----- Assert -----
            result.seriesText.shouldNotBeNull() shouldBe ""
        }

        @Test
        fun `passes the book series text through unchanged`() {
            // ----- Arrange -----
            val seriesText = "#1-2 of 6 in Dune Chronicles"
            val book = stubBook(seriesText = seriesText)

            // ----- Act -----
            val result = BookInitialCover.fromBook(book = book)

            // ----- Assert -----
            result.seriesText shouldBe seriesText
        }

        @Test
        fun `copies the title and rating from the book`() {
            // ----- Arrange -----
            val book = stubBook(
                title = "Hyperion",
                rating = 4.25,
                seriesText = null,
            )

            // ----- Act -----
            val result = BookInitialCover.fromBook(book = book)

            // ----- Assert -----
            result.title shouldBe "Hyperion"
            result.rating shouldBe 4.25
        }
    }

    @Nested
    inner class FromEdition {
        @Test
        fun `takes the title from the edition and leaves series text and rating unknown`() {
            // ----- Arrange -----
            val edition = mockk<BookEdition> {
                every {
                    title
                } returns "Dune (Deluxe)"
            }

            // ----- Act -----
            val result = BookInitialCover.fromEdition(edition = edition)

            // ----- Assert -----
            result.title shouldBe "Dune (Deluxe)"
            result.seriesText.shouldBeNull()
            result.rating.shouldBeNull()
        }
    }
}

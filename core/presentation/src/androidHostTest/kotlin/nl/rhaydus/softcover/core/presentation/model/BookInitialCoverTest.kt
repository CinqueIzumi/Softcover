package nl.rhaydus.softcover.core.presentation.model

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
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

    private fun stubEdition(editionId: Int): BookEdition = mockk {
        every {
            id
        } returns editionId
    }

    private fun stubBookWithEditions(
        editions: List<BookEdition>,
        current: BookEdition?,
    ): Book = mockk {
        every {
            this@mockk.editions
        } returns editions
        every {
            currentEdition
        } returns current
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
            title
        } returns "Dune"
        every {
            seriesText
        } returns null
        every {
            rating
        } returns 4.0
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
        fun `carries the book title, rating and series text`() {
            // ----- Arrange -----
            val seriesText = "#1-2 of 6 in Dune Chronicles"
            val book = stubBook(
                title = "Hyperion",
                rating = 4.25,
                seriesText = seriesText,
            )

            // ----- Act -----
            val result = BookInitialCover.fromBook(book = book)

            // ----- Assert -----
            result.title shouldBe "Hyperion"
            result.rating shouldBe 4.25
            result.seriesText shouldBe seriesText
        }
    }

    @Nested
    inner class ForScannedEdition {
        @Test
        fun `picks the edition matching the scanned id as the current edition`() {
            // ----- Arrange -----
            val current = stubEdition(editionId = 1)
            val scanned = stubEdition(editionId = 2)
            val book = stubBookWithEditions(
                editions = listOf(current, scanned),
                current = current,
            )

            // ----- Act -----
            val result = BookInitialCover.forScannedEdition(
                book = book,
                editionId = 2,
            )

            // ----- Assert -----
            result.currentEdition.shouldNotBeNull() shouldBeSameInstanceAs scanned
            result.scannedEditionId shouldBe 2
        }

        @Test
        fun `falls back to the book current edition when the scanned id is null`() {
            // ----- Arrange -----
            val current = stubEdition(editionId = 1)
            val book = stubBookWithEditions(
                editions = listOf(current),
                current = current,
            )

            // ----- Act -----
            val result = BookInitialCover.forScannedEdition(
                book = book,
                editionId = null,
            )

            // ----- Assert -----
            result.currentEdition.shouldNotBeNull() shouldBeSameInstanceAs current
            result.scannedEditionId.shouldBeNull()
        }

        @Test
        fun `falls back to the book current edition when no edition matches the scanned id`() {
            // ----- Arrange -----
            val current = stubEdition(editionId = 1)
            val book = stubBookWithEditions(
                editions = listOf(current),
                current = current,
            )

            // ----- Act -----
            val result = BookInitialCover.forScannedEdition(
                book = book,
                editionId = 99,
            )

            // ----- Assert -----
            result.currentEdition.shouldNotBeNull() shouldBeSameInstanceAs current
            result.scannedEditionId shouldBe 99
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

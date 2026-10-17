package nl.rhaydus.softcover.core.domain.model

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class QuoteProgressAdvanceTest {
    // ----- Fixtures -----

    private fun edition(
        pages: Int? = null,
        audioSeconds: Int? = null,
        isAudiobook: Boolean = false,
    ): BookEdition = mockk<BookEdition>().also {
        every {
            it.pages
        } returns pages

        every {
            it.audioSeconds
        } returns audioSeconds

        every {
            it.isAudiobook
        } returns isAudiobook
    }

    private fun read(
        currentPage: Int? = null,
        currentSeconds: Int? = null,
    ): UserBookRead = mockk<UserBookRead>().also {
        every {
            it.currentPage
        } returns currentPage

        every {
            it.currentSeconds
        } returns currentSeconds
    }

    private fun book(
        read: UserBookRead?,
        current: BookEdition?,
        default: BookEdition? = null,
    ): Book = mockk<Book>().also {
        every {
            it.currentRead
        } returns read

        every {
            it.currentEdition
        } returns current

        every {
            it.defaultEdition
        } returns default
    }

    @Nested
    inner class MissingContext {
        @Test
        fun `returns null when the book has no current read`() {
            // ----- Arrange -----
            val book = book(
                read = null,
                current = edition(pages = 300),
            )

            // ----- Act -----
            val result = book.progressAdvanceFor(
                position = QuotePosition.pages(
                    page = 50,
                    totalPages = 300,
                ),
            )

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `returns null when the book has no current edition`() {
            // ----- Arrange -----
            val book = book(
                read = read(currentPage = 10),
                current = null,
            )

            // ----- Act -----
            val result = book.progressAdvanceFor(
                position = QuotePosition.pages(
                    page = 50,
                    totalPages = 300,
                ),
            )

            // ----- Assert -----
            result.shouldBeNull()
        }
    }

    @Nested
    inner class PagedBook {
        @Test
        fun `advances to the quoted page when it is ahead of the current page`() {
            // ----- Arrange -----
            val book = book(
                read = read(currentPage = 10),
                current = edition(pages = 300),
            )

            // ----- Act -----
            val result = book.progressAdvanceFor(
                position = QuotePosition.pages(
                    page = 50,
                    totalPages = 300,
                ),
            )

            // ----- Assert -----
            result shouldBe QuoteProgressAdvance(
                newPage = 50,
                newSeconds = null,
                finishesBook = false,
            )
        }

        @Test
        fun `treats a read without a current page as starting at zero`() {
            // ----- Arrange -----
            val book = book(
                read = read(currentPage = null),
                current = edition(pages = 300),
            )

            // ----- Act -----
            val result = book.progressAdvanceFor(
                position = QuotePosition.pages(
                    page = 1,
                    totalPages = 300,
                ),
            )

            // ----- Assert -----
            result shouldBe QuoteProgressAdvance(
                newPage = 1,
                newSeconds = null,
                finishesBook = false,
            )
        }

        @Test
        fun `returns null when the quoted page equals the current page`() {
            // ----- Arrange -----
            val book = book(
                read = read(currentPage = 100),
                current = edition(pages = 300),
            )

            // ----- Act -----
            val result = book.progressAdvanceFor(
                position = QuotePosition.pages(
                    page = 100,
                    totalPages = 300,
                ),
            )

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `finishes the book when the quoted page is the last page of the current edition`() {
            // ----- Arrange -----
            val book = book(
                read = read(currentPage = 10),
                current = edition(pages = 300),
            )

            // ----- Act -----
            val result = book.progressAdvanceFor(
                position = QuotePosition.pages(
                    page = 300,
                    totalPages = 300,
                ),
            )

            // ----- Assert -----
            result shouldBe QuoteProgressAdvance(
                newPage = 300,
                newSeconds = null,
                finishesBook = true,
            )
        }

        @Test
        fun `does not finish the book when the page matches only the default edition's page count`() {
            // ----- Arrange -----
            val book = book(
                read = read(currentPage = 10),
                current = edition(pages = 400),
                default = edition(pages = 300),
            )

            // ----- Act -----
            val result = book.progressAdvanceFor(
                position = QuotePosition.pages(
                    page = 300,
                    totalPages = 300,
                ),
            )

            // ----- Assert -----
            result?.finishesBook shouldBe false
        }

        @Test
        fun `converts a percent position using the current edition's page count`() {
            // ----- Arrange -----
            val book = book(
                read = read(currentPage = 10),
                current = edition(pages = 200),
            )

            // ----- Act -----
            val result = book.progressAdvanceFor(position = QuotePosition.Percent(percent = 50.0))

            // ----- Assert -----
            result shouldBe QuoteProgressAdvance(
                newPage = 100,
                newSeconds = null,
                finishesBook = false,
            )
        }

        @Test
        fun `converts a percent position using the default edition's page count when the current has none`() {
            // ----- Arrange -----
            val book = book(
                read = read(currentPage = 10),
                current = edition(pages = null),
                default = edition(pages = 400),
            )

            // ----- Act -----
            val result = book.progressAdvanceFor(position = QuotePosition.Percent(percent = 25.0))

            // ----- Assert -----
            result?.newPage shouldBe 100
        }

        @Test
        fun `returns null for a percent position when no edition has a page count`() {
            // ----- Arrange -----
            val book = book(
                read = read(currentPage = 10),
                current = edition(pages = null),
                default = edition(pages = null),
            )

            // ----- Act -----
            val result = book.progressAdvanceFor(position = QuotePosition.Percent(percent = 50.0))

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `returns null for a seconds position`() {
            // ----- Arrange -----
            val book = book(
                read = read(currentPage = 10),
                current = edition(pages = 300),
            )

            // ----- Act -----
            val result = book.progressAdvanceFor(
                position = QuotePosition.seconds(
                    seconds = 600,
                    totalSeconds = 3600,
                ),
            )

            // ----- Assert -----
            result.shouldBeNull()
        }
    }

    @Nested
    inner class Audiobook {
        @Test
        fun `advances to the quoted second when it is ahead of the current second`() {
            // ----- Arrange -----
            val book = book(
                read = read(currentSeconds = 100),
                current = edition(
                    audioSeconds = 3600,
                    isAudiobook = true,
                ),
            )

            // ----- Act -----
            val result = book.progressAdvanceFor(
                position = QuotePosition.seconds(
                    seconds = 600,
                    totalSeconds = 3600,
                ),
            )

            // ----- Assert -----
            result shouldBe QuoteProgressAdvance(
                newPage = null,
                newSeconds = 600,
                finishesBook = false,
            )
        }

        @Test
        fun `returns null when the quoted second is not ahead of the current second`() {
            // ----- Arrange -----
            val book = book(
                read = read(currentSeconds = 600),
                current = edition(
                    audioSeconds = 3600,
                    isAudiobook = true,
                ),
            )

            // ----- Act -----
            val result = book.progressAdvanceFor(
                position = QuotePosition.seconds(
                    seconds = 600,
                    totalSeconds = 3600,
                ),
            )

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `finishes the book when the quoted second reaches the end of the audio`() {
            // ----- Arrange -----
            val book = book(
                read = read(currentSeconds = 100),
                current = edition(
                    audioSeconds = 3600,
                    isAudiobook = true,
                ),
            )

            // ----- Act -----
            val result = book.progressAdvanceFor(
                position = QuotePosition.seconds(
                    seconds = 3700,
                    totalSeconds = 3600,
                ),
            )

            // ----- Assert -----
            result?.finishesBook shouldBe true
        }

        @Test
        fun `converts a percent position using the current edition's audio length`() {
            // ----- Arrange -----
            val book = book(
                read = read(currentSeconds = 100),
                current = edition(
                    audioSeconds = 4000,
                    isAudiobook = true,
                ),
            )

            // ----- Act -----
            val result = book.progressAdvanceFor(position = QuotePosition.Percent(percent = 50.0))

            // ----- Assert -----
            result shouldBe QuoteProgressAdvance(
                newPage = null,
                newSeconds = 2000,
                finishesBook = false,
            )
        }

        @Test
        fun `returns null for a percent position when the edition has no audio length`() {
            // ----- Arrange -----
            val book = book(
                read = read(currentSeconds = 100),
                current = edition(
                    audioSeconds = null,
                    isAudiobook = true,
                ),
                default = edition(audioSeconds = 4000),
            )

            // ----- Act -----
            val result = book.progressAdvanceFor(position = QuotePosition.Percent(percent = 50.0))

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `returns null for a pages position`() {
            // ----- Arrange -----
            val book = book(
                read = read(currentSeconds = 100),
                current = edition(
                    audioSeconds = 3600,
                    isAudiobook = true,
                ),
            )

            // ----- Act -----
            val result = book.progressAdvanceFor(
                position = QuotePosition.pages(
                    page = 50,
                    totalPages = 300,
                ),
            )

            // ----- Assert -----
            result.shouldBeNull()
        }
    }
}

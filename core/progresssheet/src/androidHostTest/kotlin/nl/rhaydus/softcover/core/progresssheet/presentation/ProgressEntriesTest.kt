package nl.rhaydus.softcover.core.progresssheet.presentation

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.ReadingFormat
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProgressEntriesTest {
    private fun bookOf(
        edition: BookEdition?,
        defaultEdition: BookEdition? = null,
    ): Book = PreviewData.baseBook.copy(
        editions = listOfNotNull(edition),
        defaultEdition = defaultEdition,
        userBook = PreviewData.baseBook.userBook?.copy(editionId = null),
    )

    private fun pagedBook(pages: Int?): Book = bookOf(
        edition = PreviewData.baseEdition.copy(
            readingFormat = ReadingFormat.Physical,
            pages = pages,
            owned = true,
        ),
    )

    private fun audioBook(audioSeconds: Int?): Book = bookOf(
        edition = PreviewData.baseEdition.copy(
            readingFormat = ReadingFormat.Audio,
            audioSeconds = audioSeconds,
            owned = true,
        ),
    )

    @Nested
    inner class PageEntryFunction {
        @Test
        fun `parses the page and carries the actionAt`() {
            // ----- Arrange -----
            val actionAt = "2026-07-21T21:00:00Z"

            // ----- Act -----
            val entry = pageEntry(
                page = "150",
                actionAt = actionAt,
            )

            // ----- Assert -----
            entry shouldBe ProgressEntry(
                newPage = 150,
                newSeconds = null,
                actionAt = actionAt,
            )
        }

        @Test
        fun `a non-numeric page becomes 0`() {
            // ----- Arrange -----
            val page = "not-a-number"

            // ----- Act -----
            val entry = pageEntry(
                page = page,
                actionAt = null,
            )

            // ----- Assert -----
            entry.newPage shouldBe 0
        }

        @Test
        fun `a negative page passes through unclamped`() {
            // ----- Arrange -----
            val page = "-5"

            // ----- Act -----
            val entry = pageEntry(
                page = page,
                actionAt = null,
            )

            // ----- Assert -----
            entry.newPage shouldBe -5
        }
    }

    @Nested
    inner class PercentageEntryFunction {
        @Test
        fun `takes the fraction of the current edition pages`() {
            // ----- Arrange -----
            val book = pagedBook(pages = 400)

            // ----- Act -----
            val entry = percentageEntry(
                book = book,
                percentage = "25",
                actionAt = "at",
            ).shouldNotBeNull()

            // ----- Assert -----
            entry shouldBe ProgressEntry(
                newPage = 100,
                newSeconds = null,
                actionAt = "at",
            )
        }

        @Test
        fun `takes the fraction of the audio seconds for an audiobook`() {
            // ----- Arrange -----
            val book = audioBook(audioSeconds = 7200)

            // ----- Act -----
            val entry = percentageEntry(
                book = book,
                percentage = "50",
                actionAt = null,
            ).shouldNotBeNull()

            // ----- Assert -----
            entry.newSeconds shouldBe 3600
            entry.newPage.shouldBeNull()
        }

        @Test
        fun `clamps a percentage above 100 to the full length`() {
            // ----- Arrange -----
            val book = pagedBook(pages = 400)

            // ----- Act -----
            val entry = percentageEntry(
                book = book,
                percentage = "250",
                actionAt = null,
            ).shouldNotBeNull()

            // ----- Assert -----
            entry.newPage shouldBe 400
        }

        @Test
        fun `clamps a negative percentage to 0`() {
            // ----- Arrange -----
            val book = pagedBook(pages = 400)

            // ----- Act -----
            val entry = percentageEntry(
                book = book,
                percentage = "-10",
                actionAt = null,
            ).shouldNotBeNull()

            // ----- Assert -----
            entry.newPage shouldBe 0
        }

        @Test
        fun `a non-numeric percentage becomes 0`() {
            // ----- Arrange -----
            val book = pagedBook(pages = 400)

            // ----- Act -----
            val entry = percentageEntry(
                book = book,
                percentage = "abc",
                actionAt = null,
            ).shouldNotBeNull()

            // ----- Assert -----
            entry.newPage shouldBe 0
        }

        @Test
        fun `falls back to the default edition pages when the current edition has none`() {
            // ----- Arrange -----
            val book = bookOf(
                edition = PreviewData.baseEdition.copy(
                    readingFormat = ReadingFormat.Physical,
                    pages = null,
                    owned = true,
                ),
                defaultEdition = PreviewData.baseEdition.copy(pages = 200),
            )

            // ----- Act -----
            val entry = percentageEntry(
                book = book,
                percentage = "50",
                actionAt = null,
            ).shouldNotBeNull()

            // ----- Assert -----
            entry.newPage shouldBe 100
        }

        @Test
        fun `is page 0 when no edition knows its pages`() {
            // ----- Arrange -----
            val book = pagedBook(pages = null)

            // ----- Act -----
            val entry = percentageEntry(
                book = book,
                percentage = "50",
                actionAt = null,
            ).shouldNotBeNull()

            // ----- Assert -----
            entry.newPage shouldBe 0
        }

        @Test
        fun `is 0 seconds for an audiobook with unknown length`() {
            // ----- Arrange -----
            val book = audioBook(audioSeconds = null)

            // ----- Act -----
            val entry = percentageEntry(
                book = book,
                percentage = "50",
                actionAt = null,
            ).shouldNotBeNull()

            // ----- Assert -----
            entry.newSeconds shouldBe 0
        }

        @Test
        fun `is null when the book has no current edition`() {
            // ----- Arrange -----
            val book = bookOf(edition = null)

            // ----- Act -----
            val entry = percentageEntry(
                book = book,
                percentage = "50",
                actionAt = null,
            )

            // ----- Assert -----
            entry.shouldBeNull()
        }
    }

    @Nested
    inner class TimeEntryFunction {
        @Test
        fun `sums hours minutes and seconds into seconds`() {
            // ----- Arrange -----
            val book = audioBook(audioSeconds = 36000)

            // ----- Act -----
            val entry = timeEntry(
                book = book,
                hours = "1",
                minutes = "2",
                seconds = "5",
                actionAt = "at",
            )

            // ----- Assert -----
            entry shouldBe ProgressEntry(
                newPage = null,
                newSeconds = 3725,
                actionAt = "at",
            )
        }

        @Test
        fun `clamps to the known duration`() {
            // ----- Arrange -----
            val book = audioBook(audioSeconds = 3600)

            // ----- Act -----
            val entry = timeEntry(
                book = book,
                hours = "5",
                minutes = "0",
                seconds = "0",
                actionAt = null,
            )

            // ----- Assert -----
            entry.newSeconds shouldBe 3600
        }

        @Test
        fun `does not clamp above when the duration is unknown`() {
            // ----- Arrange -----
            val book = audioBook(audioSeconds = null)

            // ----- Act -----
            val entry = timeEntry(
                book = book,
                hours = "5",
                minutes = "0",
                seconds = "0",
                actionAt = null,
            )

            // ----- Assert -----
            entry.newSeconds shouldBe 18000
        }

        @Test
        fun `invalid fields count as 0`() {
            // ----- Arrange -----
            val book = audioBook(audioSeconds = 3600)

            // ----- Act -----
            val entry = timeEntry(
                book = book,
                hours = "",
                minutes = "x",
                seconds = "30",
                actionAt = null,
            )

            // ----- Assert -----
            entry.newSeconds shouldBe 30
        }

        @Test
        fun `a negative total is raised to 0 when the duration is unknown`() {
            // ----- Arrange -----
            val book = audioBook(audioSeconds = null)

            // ----- Act -----
            val entry = timeEntry(
                book = book,
                hours = "-1",
                minutes = "0",
                seconds = "0",
                actionAt = null,
            )

            // ----- Assert -----
            entry.newSeconds shouldBe 0
        }
    }
}

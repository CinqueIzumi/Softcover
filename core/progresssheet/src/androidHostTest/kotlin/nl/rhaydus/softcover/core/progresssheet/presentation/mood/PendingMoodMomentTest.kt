package nl.rhaydus.softcover.core.progresssheet.presentation.mood

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ReadingFormat
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.core.progresssheet.presentation.ProgressEntry
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PendingMoodMomentTest {
    private fun pagedBook(pages: Int?): Book = PreviewData.baseBook.copy(
        editions = listOf(
            PreviewData.baseEdition.copy(
                readingFormat = ReadingFormat.Physical,
                pages = pages,
                owned = true,
            ),
        ),
        defaultEdition = null,
        userBook = PreviewData.baseBook.userBook?.copy(editionId = null),
    )

    private fun audioBook(audioSeconds: Int?): Book = PreviewData.baseBook.copy(
        editions = listOf(
            PreviewData.baseEdition.copy(
                readingFormat = ReadingFormat.Audio,
                audioSeconds = audioSeconds,
                owned = true,
            ),
        ),
        defaultEdition = null,
        userBook = PreviewData.baseBook.userBook?.copy(editionId = null),
    )

    private fun pageEntry(page: Int) = ProgressEntry(
        newPage = page,
        newSeconds = null,
        actionAt = null,
    )

    private fun secondsEntry(seconds: Int) = ProgressEntry(
        newPage = null,
        newSeconds = seconds,
        actionAt = null,
    )

    @Nested
    inner class PageEntries {
        @Test
        fun `computes the percent from the page over the total pages`() {
            // ----- Arrange -----
            val book = pagedBook(300)

            // ----- Act -----
            val result = pageEntry(150).toPendingMoodMoment(book)

            // ----- Assert -----
            result shouldBe PendingMoodMoment(
                bookId = book.id,
                page = 150,
                seconds = null,
                percent = 50,
            )
        }

        @Test
        fun `rounds to the nearest percent`() {
            // ----- Arrange -----
            val book = pagedBook(300)

            // ----- Act -----
            val oneThird = pageEntry(100).toPendingMoodMoment(book)
            val twoThirds = pageEntry(200).toPendingMoodMoment(book)

            // ----- Assert -----
            oneThird.percent shouldBe 33
            twoThirds.percent shouldBe 67
        }

        @Test
        fun `clamps a page past the end to 100`() {
            // ----- Arrange -----
            val book = pagedBook(300)

            // ----- Act -----
            val result = pageEntry(450).toPendingMoodMoment(book)

            // ----- Assert -----
            result.percent shouldBe 100
        }

        @Test
        fun `clamps a negative page to 0`() {
            // ----- Arrange -----
            val book = pagedBook(300)

            // ----- Act -----
            val result = pageEntry(-5).toPendingMoodMoment(book)

            // ----- Assert -----
            result.percent shouldBe 0
        }

        @Test
        fun `has no percent when the total pages are unknown`() {
            // ----- Arrange -----
            val book = pagedBook(null)

            // ----- Act -----
            val result = pageEntry(150).toPendingMoodMoment(book)

            // ----- Assert -----
            result.percent.shouldBeNull()
        }

        @Test
        fun `has no percent when the total pages are zero`() {
            // ----- Arrange -----
            val book = pagedBook(0)

            // ----- Act -----
            val result = pageEntry(150).toPendingMoodMoment(book)

            // ----- Assert -----
            result.percent.shouldBeNull()
        }
    }

    @Nested
    inner class AudioEntries {
        @Test
        fun `computes the percent from seconds over the edition's audio length`() {
            // ----- Arrange -----
            val book = audioBook(7200)

            // ----- Act -----
            val result = secondsEntry(1800).toPendingMoodMoment(book)

            // ----- Assert -----
            result shouldBe PendingMoodMoment(
                bookId = book.id,
                page = null,
                seconds = 1800,
                percent = 25,
            )
        }

        @Test
        fun `clamps seconds past the end to 100`() {
            // ----- Arrange -----
            val book = audioBook(7200)

            // ----- Act -----
            val result = secondsEntry(9000).toPendingMoodMoment(book)

            // ----- Assert -----
            result.percent shouldBe 100
        }

        @Test
        fun `has no percent when the audio length is unknown`() {
            // ----- Arrange -----
            val book = audioBook(null)

            // ----- Act -----
            val result = secondsEntry(1800).toPendingMoodMoment(book)

            // ----- Assert -----
            result.percent.shouldBeNull()
        }
    }
}

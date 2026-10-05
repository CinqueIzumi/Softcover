package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.common.secondsToHm
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.ReadingFormat
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.feature.book_detail.presentation.buildEdition
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class InProgressMapperTest {
    private fun stubBook(
        read: UserBookRead?,
        edition: BookEdition?,
    ): Book = mockk<Book> {
        every {
            userBookRead
        } returns read

        every {
            currentEdition
        } returns edition

        every {
            defaultEdition
        } returns null
    }

    private fun buildRead(
        progress: Float = 0f,
        currentPage: Int? = null,
        currentSeconds: Int? = null,
    ): UserBookRead = UserBookRead(
        id = 1,
        currentPage = currentPage,
        currentSeconds = currentSeconds,
        progress = progress,
        startedAt = null,
        finishedAt = null,
    )

    private fun paperEdition(pages: Int?): BookEdition = buildEdition(id = 1).copy(
        pages = pages,
        readingFormat = ReadingFormat.Physical,
    )

    private fun audioEdition(audioSeconds: Int?): BookEdition = buildEdition(id = 1).copy(
        audioSeconds = audioSeconds,
        readingFormat = ReadingFormat.Audio,
    )

    @Nested
    inner class ToInProgress {
        @Test
        fun `returns null when the book has no read`() {
            // ----- Arrange -----
            val book = stubBook(
                read = null,
                edition = paperEdition(pages = 300),
            )

            // ----- Act -----
            val result = book.toInProgress()

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `returns null when the book has no current edition`() {
            // ----- Arrange -----
            val book = stubBook(
                read = buildRead(progress = 50f),
                edition = null,
            )

            // ----- Act -----
            val result = book.toInProgress()

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `percent label rounds the progress to the nearest whole number`() {
            // ----- Arrange -----
            val book = stubBook(
                read = buildRead(progress = 42.6f),
                edition = paperEdition(pages = 300),
            )

            // ----- Act -----
            val result = book.toInProgress()

            // ----- Assert -----
            result.shouldNotBeNull().percent shouldBe "43%"
        }

        @Test
        fun `bar fraction is the progress as a share of one`() {
            // ----- Arrange -----
            val book = stubBook(
                read = buildRead(progress = 25f),
                edition = paperEdition(pages = 300),
            )

            // ----- Act -----
            val result = book.toInProgress()

            // ----- Assert -----
            result.shouldNotBeNull().fraction shouldBe 0.25f
        }

        @Test
        fun `bar fraction passes progress above 100 through unclamped`() {
            // ----- Arrange -----
            val book = stubBook(
                read = buildRead(progress = 130f),
                edition = paperEdition(pages = 300),
            )

            // ----- Act -----
            val result = book.toInProgress()

            // ----- Assert -----
            result.shouldNotBeNull().fraction shouldBe 1.3f
        }

        @Test
        fun `paper book summarises the current page and the pages to go`() {
            // ----- Arrange -----
            val book = stubBook(
                read = buildRead(
                    progress = 10f,
                    currentPage = 30,
                ),
                edition = paperEdition(pages = 300),
            )

            // ----- Act -----
            val result = book.toInProgress()

            // ----- Assert -----
            val model = result.shouldNotBeNull()
            model.summaryStart shouldBe "p. 30 of 300"
            model.summaryEnd shouldBe "270 pages to go"
        }

        @Test
        fun `paper book without a current page starts from page zero`() {
            // ----- Arrange -----
            val book = stubBook(
                read = buildRead(currentPage = null),
                edition = paperEdition(pages = 300),
            )

            // ----- Act -----
            val result = book.toInProgress()

            // ----- Assert -----
            val model = result.shouldNotBeNull()
            model.summaryStart shouldBe "p. 0 of 300"
            model.summaryEnd shouldBe "300 pages to go"
        }

        @Test
        fun `paper book with unknown page total reports zero pages to go`() {
            // ----- Arrange -----
            val book = stubBook(
                read = buildRead(currentPage = 30),
                edition = paperEdition(pages = null),
            )

            // ----- Act -----
            val result = book.toInProgress()

            // ----- Assert -----
            val model = result.shouldNotBeNull()
            model.summaryStart shouldBe "p. 30 of 0"
            model.summaryEnd shouldBe "0 pages to go"
        }

        @Test
        fun `audiobook summarises the listened time, the total and the time left`() {
            // ----- Arrange -----
            val book = stubBook(
                read = buildRead(currentSeconds = 3_600),
                edition = audioEdition(audioSeconds = 10_800),
            )

            // ----- Act -----
            val result = book.toInProgress()

            // ----- Assert -----
            val model = result.shouldNotBeNull()
            model.summaryStart shouldBe "${secondsToHm(3_600)} of ${secondsToHm(10_800)}"
            model.summaryEnd shouldBe "${secondsToHm(7_200)} left"
        }

        @Test
        fun `audiobook ignores the page fields`() {
            // ----- Arrange -----
            val book = stubBook(
                read = buildRead(
                    currentPage = 30,
                    currentSeconds = 3_600,
                ),
                edition = audioEdition(audioSeconds = 10_800).copy(pages = 300),
            )

            // ----- Act -----
            val result = book.toInProgress()

            // ----- Assert -----
            val model = result.shouldNotBeNull()
            model.summaryStart.contains("p. ") shouldBe false
            model.summaryEnd.contains("pages to go") shouldBe false
        }

        @Test
        fun `audiobook time left is clamped to zero when listened time exceeds the total`() {
            // ----- Arrange -----
            val book = stubBook(
                read = buildRead(currentSeconds = 12_000),
                edition = audioEdition(audioSeconds = 10_800),
            )

            // ----- Act -----
            val result = book.toInProgress()

            // ----- Assert -----
            result.shouldNotBeNull().summaryEnd shouldBe "${secondsToHm(0)} left"
        }

        @Test
        fun `audiobook with unknown durations falls back to zero`() {
            // ----- Arrange -----
            val book = stubBook(
                read = buildRead(currentSeconds = null),
                edition = audioEdition(audioSeconds = null),
            )

            // ----- Act -----
            val result = book.toInProgress()

            // ----- Assert -----
            val model = result.shouldNotBeNull()
            model.summaryStart shouldBe "${secondsToHm(0)} of ${secondsToHm(0)}"
            model.summaryEnd shouldBe "${secondsToHm(0)} left"
        }
    }
}

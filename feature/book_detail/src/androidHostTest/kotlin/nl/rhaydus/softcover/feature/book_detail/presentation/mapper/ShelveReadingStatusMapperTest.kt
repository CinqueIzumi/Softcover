package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.component.statistic.StatNumberFormat
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.ReadingFormat
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.feature.book_detail.presentation.buildEdition
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ShelveReadingStatusMapperTest {
    private fun stubBook(
        read: UserBookRead?,
        edition: BookEdition?,
    ): Book = mockk<Book> {
        every {
            currentRead
        } returns read

        every {
            currentEdition
        } returns edition
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
        finishedAtPrecision = null,
        editionPages = null,
    )

    private fun paperEdition(): BookEdition = buildEdition(id = 1).copy(
        pages = 300,
        readingFormat = ReadingFormat.Physical,
    )

    private fun audioEdition(): BookEdition = buildEdition(id = 1).copy(
        audioSeconds = 10_800,
        readingFormat = ReadingFormat.Audio,
    )

    @Nested
    inner class ToShelveReadingStatus {
        @Test
        fun `returns null when the book has no read`() {
            // ----- Arrange -----
            val book = stubBook(
                read = null,
                edition = paperEdition(),
            )

            // ----- Act -----
            val result = book.toShelveReadingStatus()

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `percent rounds the progress to the nearest whole number`() {
            // ----- Arrange -----
            val book = stubBook(
                read = buildRead(progress = 42.6f),
                edition = paperEdition(),
            )

            // ----- Act -----
            val result = book.toShelveReadingStatus()

            // ----- Assert -----
            result.shouldNotBeNull().percent.value shouldBe 43.0
        }

        @Test
        fun `percent caps progress above 100 at 100`() {
            // ----- Arrange -----
            val book = stubBook(
                read = buildRead(progress = 140f),
                edition = paperEdition(),
            )

            // ----- Act -----
            val result = book.toShelveReadingStatus()

            // ----- Assert -----
            result.shouldNotBeNull().percent.value shouldBe 100.0
        }

        @Test
        fun `percent floors negative progress at 0`() {
            // ----- Arrange -----
            val book = stubBook(
                read = buildRead(progress = -15f),
                edition = paperEdition(),
            )

            // ----- Act -----
            val result = book.toShelveReadingStatus()

            // ----- Assert -----
            result.shouldNotBeNull().percent.value shouldBe 0.0
        }

        @Test
        fun `book position is the current page`() {
            // ----- Arrange -----
            val book = stubBook(
                read = buildRead(
                    progress = 10f,
                    currentPage = 30,
                    currentSeconds = 99,
                ),
                edition = paperEdition(),
            )

            // ----- Act -----
            val result = book.toShelveReadingStatus()

            // ----- Assert -----
            val position = result.shouldNotBeNull().position
            position.value shouldBe 30.0
            position.format shouldBe StatNumberFormat.Plain
        }

        @Test
        fun `book without a current page starts from zero`() {
            // ----- Arrange -----
            val book = stubBook(
                read = buildRead(currentPage = null),
                edition = paperEdition(),
            )

            // ----- Act -----
            val result = book.toShelveReadingStatus()

            // ----- Assert -----
            result.shouldNotBeNull().position.value shouldBe 0.0
        }

        @Test
        fun `book without a current edition is treated as a book`() {
            // ----- Arrange -----
            val book = stubBook(
                read = buildRead(
                    currentPage = 12,
                    currentSeconds = 99,
                ),
                edition = null,
            )

            // ----- Act -----
            val result = book.toShelveReadingStatus()

            // ----- Assert -----
            val position = result.shouldNotBeNull().position
            position.value shouldBe 12.0
            position.format shouldBe StatNumberFormat.Plain
        }

        @Test
        fun `audiobook position is the listened seconds as a duration`() {
            // ----- Arrange -----
            val book = stubBook(
                read = buildRead(
                    currentPage = 30,
                    currentSeconds = 3_600,
                ),
                edition = audioEdition(),
            )

            // ----- Act -----
            val result = book.toShelveReadingStatus()

            // ----- Assert -----
            val position = result.shouldNotBeNull().position
            position.value shouldBe 3_600.0
            position.format shouldBe StatNumberFormat.Duration
        }

        @Test
        fun `audiobook without listened seconds starts from zero`() {
            // ----- Arrange -----
            val book = stubBook(
                read = buildRead(currentSeconds = null),
                edition = audioEdition(),
            )

            // ----- Act -----
            val result = book.toShelveReadingStatus()

            // ----- Assert -----
            result.shouldNotBeNull().position.value shouldBe 0.0
        }
    }
}

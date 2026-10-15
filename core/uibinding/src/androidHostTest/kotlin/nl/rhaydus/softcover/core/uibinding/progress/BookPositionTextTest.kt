package nl.rhaydus.softcover.core.uibinding.progress

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.ReadingFormat
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookPositionTextTest {
    private fun read(
        currentPage: Int? = null,
        currentSeconds: Int? = null,
    ): UserBookRead = PreviewData.baseBook.userBookRead.shouldNotBeNull().copy(
        currentPage = currentPage,
        currentSeconds = currentSeconds,
    )

    @Nested
    inner class Paged {
        @Test
        fun `reports the page position and the pages to go`() {
            // ----- Arrange -----
            val subject = read(currentPage = 40)

            // ----- Act -----
            val result = subject.toPositionText(
                edition = PreviewData.baseEdition,
                totalPages = 300,
            )

            // ----- Assert -----
            result shouldBe BookPositionText(
                position = "p. 40 of 300",
                remaining = "260 pages to go",
            )
        }

        @Test
        fun `a position past the end clamps the pages to go at zero`() {
            // ----- Arrange -----
            val subject = read(currentPage = 320)

            // ----- Act -----
            val result = subject.toPositionText(
                edition = PreviewData.baseEdition,
                totalPages = 300,
            )

            // ----- Assert -----
            result.position shouldBe "p. 320 of 300"
            result.remaining shouldBe "0 pages to go"
        }

        @Test
        fun `a read with no page starts at page zero`() {
            // ----- Arrange -----
            val subject = read(currentPage = null)

            // ----- Act -----
            val result = subject.toPositionText(
                edition = PreviewData.baseEdition,
                totalPages = 300,
            )

            // ----- Assert -----
            result shouldBe BookPositionText(
                position = "p. 0 of 300",
                remaining = "300 pages to go",
            )
        }
    }

    @Nested
    inner class PausedPaged {
        @Test
        fun `reports the pages left instead of the pages to go`() {
            // ----- Arrange -----
            val subject = read(currentPage = 40)

            // ----- Act -----
            val result = subject.toPositionText(
                edition = PreviewData.baseEdition,
                totalPages = 300,
                isPaused = true,
            )

            // ----- Assert -----
            result shouldBe BookPositionText(
                position = "p. 40 of 300",
                remaining = "260 pages left",
            )
        }

        @Test
        fun `a position past the end clamps the pages left at zero`() {
            // ----- Arrange -----
            val subject = read(currentPage = 320)

            // ----- Act -----
            val result = subject.toPositionText(
                edition = PreviewData.baseEdition,
                totalPages = 300,
                isPaused = true,
            )

            // ----- Assert -----
            result.remaining shouldBe "0 pages left"
        }

        @Test
        fun `an audiobook keeps the time left wording`() {
            // ----- Arrange -----
            val audioEdition = PreviewData.baseEdition.copy(
                pages = null,
                audioSeconds = 7_200,
                readingFormat = ReadingFormat.Audio,
            )
            val subject = read(currentSeconds = 1_800)

            // ----- Act -----
            val result = subject.toPositionText(
                edition = audioEdition,
                totalPages = 0,
                isPaused = true,
            )

            // ----- Assert -----
            result.remaining shouldBe "1h 30m left"
        }
    }

    @Nested
    inner class Audio {
        private val audioEdition = PreviewData.baseEdition.copy(
            pages = null,
            audioSeconds = 7_200,
            readingFormat = ReadingFormat.Audio,
        )

        @Test
        fun `reports the listening position and the time left`() {
            // ----- Arrange -----
            val subject = read(currentSeconds = 1_800)

            // ----- Act -----
            val result = subject.toPositionText(
                edition = audioEdition,
                totalPages = 0,
            )

            // ----- Assert -----
            result shouldBe BookPositionText(
                position = "0h 30m of 2h 0m",
                remaining = "1h 30m left",
            )
        }

        @Test
        fun `a position past the end clamps the time left at zero`() {
            // ----- Arrange -----
            val subject = read(currentSeconds = 9_000)

            // ----- Act -----
            val result = subject.toPositionText(
                edition = audioEdition,
                totalPages = 0,
            )

            // ----- Assert -----
            result.remaining shouldBe "0h 0m left"
        }
    }

    @Nested
    inner class BookOverload {
        @Test
        fun `is null without a read`() {
            // ----- Arrange -----
            val subject = PreviewData.baseBook.copy(userBookRead = null)

            // ----- Act -----
            val result = subject.toPositionText()

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `measures pages against the default edition when the current edition has none`() {
            // ----- Arrange -----
            val current = PreviewData.baseEdition.copy(
                id = 10,
                pages = null,
                readingFormat = ReadingFormat.Physical,
            )
            val default = PreviewData.baseEdition.copy(
                id = 11,
                pages = 250,
                readingFormat = ReadingFormat.Physical,
            )
            val subject = PreviewData.baseBook.copy(
                editions = listOf(current),
                defaultEdition = default,
                userBook = PreviewData.baseBook.userBook?.copy(editionId = current.id),
                userBookRead = read(currentPage = 50),
            )

            // ----- Act -----
            val result = subject.toPositionText()

            // ----- Assert -----
            subject.resolvedTotalPages() shouldBe 250
            result shouldBe BookPositionText(
                position = "p. 50 of 250",
                remaining = "200 pages to go",
            )
        }

        @Test
        fun `a paused book reports pages left`() {
            // ----- Arrange -----
            val paused = PreviewData.baseBook.userBook.shouldNotBeNull().copy(status = BookStatus.Paused)
            val edition = PreviewData.baseEdition.copy(
                pages = 300,
                readingFormat = ReadingFormat.Physical,
            )
            val subject = PreviewData.baseBook.copy(
                editions = listOf(edition),
                userBook = paused.copy(editionId = edition.id),
                userBookRead = read(currentPage = 100),
            )

            // ----- Act -----
            val result = subject.toPositionText()

            // ----- Assert -----
            result.shouldNotBeNull().remaining shouldBe "200 pages left"
        }

        @Test
        fun `a reading book reports pages to go`() {
            // ----- Arrange -----
            val reading = PreviewData.baseBook.userBook.shouldNotBeNull().copy(status = BookStatus.Reading)
            val edition = PreviewData.baseEdition.copy(
                pages = 300,
                readingFormat = ReadingFormat.Physical,
            )
            val subject = PreviewData.baseBook.copy(
                editions = listOf(edition),
                userBook = reading.copy(editionId = edition.id),
                userBookRead = read(currentPage = 100),
            )

            // ----- Act -----
            val result = subject.toPositionText()

            // ----- Assert -----
            result.shouldNotBeNull().remaining shouldBe "200 pages to go"
        }
    }
}

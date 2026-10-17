package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class YourReadsMapperTest {
    private val dateStyle = DateStyle.YEAR_MONTH_DAY

    private fun read(
        id: Int,
        startedAt: String? = null,
        finishedAt: String? = null,
        progress: Float = 0f,
    ): UserBookRead = UserBookRead(
        id = id,
        currentPage = null,
        currentSeconds = null,
        progress = progress,
        startedAt = startedAt,
        finishedAt = finishedAt,
    )

    private fun userBook(
        status: BookStatus,
        reads: List<UserBookRead>,
    ): UserBook = UserBook(
        id = 1,
        status = status,
        dateAdded = "2026-01-01",
        createdAt = null,
        privacySettingId = 1,
        reviewHasSpoilers = false,
        editionId = null,
        lastReadDate = null,
        rating = null,
        referrerUserId = null,
        reviewedAt = null,
        updatedAt = null,
        journals = emptyList(),
        reads = reads,
    )

    private fun book(
        status: BookStatus,
        reads: List<UserBookRead>,
    ): Book = mockk {
        every {
            this@mockk.status
        } returns status

        every {
            userBook
        } returns userBook(
            status = status,
            reads = reads,
        )
    }

    @Nested
    inner class ToYourReads {
        @Test
        fun `returns null when the book has no user book`() {
            // ----- Arrange -----
            val book = mockk<Book> {
                every {
                    userBook
                } returns null
            }

            // ----- Act -----
            val result = book.toYourReads(dateStyle = dateStyle)

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `returns null when the user book has no reads`() {
            // ----- Arrange -----
            val book = book(
                status = BookStatus.WantToRead,
                reads = emptyList(),
            )

            // ----- Act -----
            val result = book.toYourReads(dateStyle = dateStyle)

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `summarises finished reads with distinct ascending years`() {
            // ----- Arrange -----
            val book = book(
                status = BookStatus.Read,
                reads = listOf(
                    read(
                        id = 1,
                        finishedAt = "2026-03-01",
                    ),
                    read(
                        id = 2,
                        finishedAt = "2025-05-01",
                    ),
                    read(
                        id = 3,
                        finishedAt = "2026-08-01",
                    ),
                ),
            )

            // ----- Act -----
            val result = book.toYourReads(dateStyle = dateStyle).shouldNotBeNull()

            // ----- Assert -----
            result.summary shouldBe "Read 3× · 2025, 2026"
        }

        @Test
        fun `summary is null when no read is finished`() {
            // ----- Arrange -----
            val book = book(
                status = BookStatus.Reading,
                reads = listOf(read(
                    id = 1,
                    startedAt = "2026-01-02",
                ),),
            )

            // ----- Act -----
            val result = book.toYourReads(dateStyle = dateStyle).shouldNotBeNull()

            // ----- Assert -----
            result.summary.shouldBeNull()
        }

        @Test
        fun `orders rows with the placeholder first and then newest id first`() {
            // ----- Arrange -----
            val book = book(
                status = BookStatus.Reading,
                reads = listOf(
                    read(id = 2),
                    read(id = -1),
                    read(id = 7),
                ),
            )

            // ----- Act -----
            val result = book.toYourReads(dateStyle = dateStyle).shouldNotBeNull()

            // ----- Assert -----
            result.rows.map { it.key } shouldBe listOf(-1, 7, 2)
        }
    }

    @Nested
    inner class RowTitle {
        @Test
        fun `titles the current read of a Reading book as Reading now`() {
            // ----- Arrange -----
            val book = book(
                status = BookStatus.Reading,
                reads = listOf(read(id = 1)),
            )

            // ----- Act -----
            val result = book.toYourReads(dateStyle = dateStyle).shouldNotBeNull()

            // ----- Assert -----
            result.rows.single().title shouldBe "Reading now"
        }

        @Test
        fun `titles the current read of a Paused book as On hold`() {
            // ----- Arrange -----
            val book = book(
                status = BookStatus.Paused,
                reads = listOf(read(id = 1)),
            )

            // ----- Act -----
            val result = book.toYourReads(dateStyle = dateStyle).shouldNotBeNull()

            // ----- Assert -----
            result.rows.single().title shouldBe "On hold"
        }

        @Test
        fun `titles a finished read as Finished`() {
            // ----- Arrange -----
            val book = book(
                status = BookStatus.Read,
                reads = listOf(read(
                    id = 1,
                    finishedAt = "2026-02-02",
                ),),
            )

            // ----- Act -----
            val result = book.toYourReads(dateStyle = dateStyle).shouldNotBeNull()

            // ----- Assert -----
            result.rows.single().title shouldBe "Finished"
        }

        @Test
        fun `titles an unfinished read that is not current as Not finished`() {
            // ----- Arrange -----
            val book = book(
                status = BookStatus.Reading,
                reads = listOf(
                    read(id = 1),
                    read(id = 2),
                ),
            )

            // ----- Act -----
            val result = book.toYourReads(dateStyle = dateStyle).shouldNotBeNull()

            // ----- Assert -----
            result.rows shouldHaveSize 2
            result.rows.map { it.title } shouldBe listOf("Reading now", "Not finished")
        }
    }

    @Nested
    inner class RowSupporting {
        @Test
        fun `shows the start and finish dates as a range`() {
            // ----- Arrange -----
            val book = book(
                status = BookStatus.Read,
                reads = listOf(
                    read(
                        id = 1,
                        startedAt = "2026-01-05",
                        finishedAt = "2026-02-10",
                    ),
                ),
            )

            // ----- Act -----
            val result = book.toYourReads(dateStyle = dateStyle).shouldNotBeNull()

            // ----- Assert -----
            result.rows.single().supporting shouldBe "2026/01/05 – 2026/02/10"
        }

        @Test
        fun `shows only the finish date when there is no start date`() {
            // ----- Arrange -----
            val book = book(
                status = BookStatus.Read,
                reads = listOf(read(
                    id = 1,
                    finishedAt = "2026-02-10",
                ),),
            )

            // ----- Act -----
            val result = book.toYourReads(dateStyle = dateStyle).shouldNotBeNull()

            // ----- Assert -----
            result.rows.single().supporting shouldBe "Finished 2026/02/10"
        }

        @Test
        fun `shows only the start date when the read is unfinished`() {
            // ----- Arrange -----
            val book = book(
                status = BookStatus.Reading,
                reads = listOf(read(
                    id = 1,
                    startedAt = "2026-01-05",
                ),),
            )

            // ----- Act -----
            val result = book.toYourReads(dateStyle = dateStyle).shouldNotBeNull()

            // ----- Assert -----
            result.rows.single().supporting shouldBe "Started 2026/01/05"
        }

        @Test
        fun `shows No dates when the read has neither date`() {
            // ----- Arrange -----
            val book = book(
                status = BookStatus.Reading,
                reads = listOf(read(id = 1)),
            )

            // ----- Act -----
            val result = book.toYourReads(dateStyle = dateStyle).shouldNotBeNull()

            // ----- Assert -----
            result.rows.single().supporting shouldBe "No dates"
        }

        @Test
        fun `treats an unparseable date as missing`() {
            // ----- Arrange -----
            val book = book(
                status = BookStatus.Reading,
                reads = listOf(
                    read(
                        id = 1,
                        startedAt = "not-a-date",
                        finishedAt = "2026-02-10",
                    ),
                ),
            )

            // ----- Act -----
            val result = book.toYourReads(dateStyle = dateStyle).shouldNotBeNull()

            // ----- Assert -----
            result.rows.single().supporting shouldBe "Finished 2026/02/10"
        }

        @Test
        fun `formats dates in the requested style`() {
            // ----- Arrange -----
            val book = book(
                status = BookStatus.Reading,
                reads = listOf(read(
                    id = 1,
                    startedAt = "2026-01-05",
                ),),
            )

            // ----- Act -----
            val result = book.toYourReads(dateStyle = DateStyle.DAY_MONTH_YEAR).shouldNotBeNull()

            // ----- Assert -----
            result.rows.single().supporting shouldBe "Started 05/01/2026"
        }
    }

    @Nested
    inner class RowTrailing {
        @Test
        fun `rounds the 0 to 100 progress to a whole percent`() {
            // ----- Arrange -----
            val book = book(
                status = BookStatus.Reading,
                reads = listOf(read(
                    id = 1,
                    progress = 13.6f,
                ),),
            )

            // ----- Act -----
            val result = book.toYourReads(dateStyle = dateStyle).shouldNotBeNull()

            // ----- Assert -----
            result.rows.single().trailing shouldBe "14%"
        }

        @Test
        fun `shows a completed read as 100 percent`() {
            // ----- Arrange -----
            val book = book(
                status = BookStatus.Read,
                reads = listOf(read(
                    id = 1,
                    finishedAt = "2026-02-10",
                    progress = 100f,
                ),),
            )

            // ----- Act -----
            val result = book.toYourReads(dateStyle = dateStyle).shouldNotBeNull()

            // ----- Assert -----
            result.rows.single().trailing shouldBe "100%"
        }
    }
}

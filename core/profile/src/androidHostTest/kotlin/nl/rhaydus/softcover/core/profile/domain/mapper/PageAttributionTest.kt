package nl.rhaydus.softcover.core.profile.domain.mapper

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import nl.rhaydus.softcover.core.book.domain.model.ProgressUpdate
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Instant

class PageAttributionTest {
    private fun update(
        bookId: Int,
        at: String,
        pagesRead: Int,
        pages: Int? = null,
    ) = ProgressUpdate(
        bookId = bookId,
        at = Instant.parse(at),
        pages = pages,
        pagesRead = pagesRead,
    )

    @Nested
    inner class AttributePages {
        @Test
        fun `credits the unlogged remainder to the finish date`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 1,
                    at = "2026-03-10T10:00:00Z",
                    pagesRead = 100,
                    pages = 100,
                ),
            )

            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-03-12",
                    pages = 300,
                ),
            )

            // ----- Act -----
            val result = attributePages(
                updates = updates,
                readBooks = books,
                didNotFinishBooks = emptyList(),
                timeZone = TimeZone.UTC,
            )

            // ----- Assert -----
            result shouldContainExactlyInAnyOrder listOf(
                AttributedPages(
                    date = LocalDate(
                        year = 2026,
                        month = 3,
                        day = 10,
                    ),
                    bookId = 1,
                    pages = 100,
                    kind = AttributedPagesKind.Update,
                ),
                AttributedPages(
                    date = LocalDate(
                        year = 2026,
                        month = 3,
                        day = 12,
                    ),
                    bookId = 1,
                    pages = 200,
                    kind = AttributedPagesKind.FinishRemainder(endPage = 300),
                ),
            )
        }

        @Test
        fun `credits all pages on the finish date of a finished book with no updates`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-03-12",
                    pages = 300,
                ),
            )

            // ----- Act -----
            val result = attributePages(
                updates = emptyList(),
                readBooks = books,
                didNotFinishBooks = emptyList(),
                timeZone = TimeZone.UTC,
            )

            // ----- Assert -----
            result.map { it.pages } shouldBe listOf(300)
        }

        @Test
        fun `drops a negative remainder when the last position is past the page count`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 1,
                    at = "2026-03-10T10:00:00Z",
                    pagesRead = 350,
                    pages = 350,
                ),
            )

            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-03-12",
                    pages = 300,
                ),
            )

            // ----- Act -----
            val result = attributePages(
                updates = updates,
                readBooks = books,
                didNotFinishBooks = emptyList(),
                timeZone = TimeZone.UTC,
            )

            // ----- Assert -----
            result.map { it.kind to it.pages } shouldBe listOf(AttributedPagesKind.Update to 350)
        }

        @Test
        fun `drops updates that added no pages`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 1,
                    at = "2026-03-10T10:00:00Z",
                    pagesRead = 0,
                ),
                update(
                    bookId = 1,
                    at = "2026-03-10T11:00:00Z",
                    pagesRead = 0,
                    pages = 40,
                ),
            )

            // ----- Act -----
            val result = attributePages(
                updates = updates,
                readBooks = emptyList(),
                didNotFinishBooks = emptyList(),
                timeZone = TimeZone.UTC,
            )

            // ----- Assert -----
            result.shouldBeEmpty()
        }

        @Test
        fun `attributes updates of a book that is not in the read books`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 9,
                    at = "2026-03-10T10:00:00Z",
                    pagesRead = 25,
                    pages = 25,
                ),
            )

            // ----- Act -----
            val result = attributePages(
                updates = updates,
                readBooks = emptyList(),
                didNotFinishBooks = emptyList(),
                timeZone = TimeZone.UTC,
            )

            // ----- Assert -----
            result.map { it.bookId to it.pages } shouldBe listOf(9 to 25)
        }

        @Test
        fun `buckets an update by its local date across midnight`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 1,
                    at = "2026-06-10T22:30:00Z",
                    pagesRead = 30,
                    pages = 30,
                ),
            )

            // ----- Act -----
            val result = attributePages(
                updates = updates,
                readBooks = emptyList(),
                didNotFinishBooks = emptyList(),
                timeZone = TimeZone.of("Europe/Amsterdam"),
            )

            // ----- Assert -----
            result.map { it.date } shouldBe listOf(
                LocalDate(
                    year = 2026,
                    month = 6,
                    day = 11,
                ),
            )
        }

        @Test
        fun `credits a stopped book the pages between its last logged position and its stop page`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 2,
                    at = "2026-03-01T10:00:00Z",
                    pagesRead = 100,
                    pages = 100,
                ),
            )

            val stopped = dnfBook(
                id = 2,
                stoppedAt = "2026-03-12T12:00:00.000000",
                pages = 400,
                read = unfinishedRead(
                    id = 2,
                    currentPage = 160,
                ),
            )

            // ----- Act -----
            val result = attributePages(
                updates = updates,
                readBooks = emptyList(),
                didNotFinishBooks = listOf(stopped),
                timeZone = TimeZone.UTC,
            )

            // ----- Assert -----
            result shouldContainExactlyInAnyOrder listOf(
                AttributedPages(
                    date = LocalDate(
                        year = 2026,
                        month = 3,
                        day = 1,
                    ),
                    bookId = 2,
                    pages = 100,
                    kind = AttributedPagesKind.Update,
                ),
                AttributedPages(
                    date = LocalDate(
                        year = 2026,
                        month = 3,
                        day = 12,
                    ),
                    bookId = 2,
                    pages = 60,
                    kind = AttributedPagesKind.StopRemainder(endPage = 160),
                ),
            )
        }

        @Test
        fun `credits a stopped book with no updates all pages up to its stop page`() {
            // ----- Arrange -----
            val stopped = dnfBook(
                id = 2,
                pages = 400,
                read = unfinishedRead(
                    id = 2,
                    currentPage = 160,
                ),
            )

            // ----- Act -----
            val result = attributePages(
                updates = emptyList(),
                readBooks = emptyList(),
                didNotFinishBooks = listOf(stopped),
                timeZone = TimeZone.UTC,
            )

            // ----- Assert -----
            result.map { it.pages } shouldBe listOf(160)
        }

        @Test
        fun `ignores logged positions after the stop date when measuring the stop remainder`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 2,
                    at = "2026-03-20T10:00:00Z",
                    pagesRead = 150,
                    pages = 150,
                ),
            )

            val stopped = dnfBook(
                id = 2,
                stoppedAt = "2026-03-12T12:00:00.000000",
                pages = 400,
                read = unfinishedRead(
                    id = 2,
                    currentPage = 160,
                ),
            )

            // ----- Act -----
            val result = attributePages(
                updates = updates,
                readBooks = emptyList(),
                didNotFinishBooks = listOf(stopped),
                timeZone = TimeZone.UTC,
            )

            // ----- Assert -----
            result.filter { it.kind is AttributedPagesKind.StopRemainder }.map { it.pages } shouldBe listOf(160)
        }

        @Test
        fun `credits no stop remainder when the book is logged up to or past its stop page`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 2,
                    at = "2026-03-01T10:00:00Z",
                    pagesRead = 200,
                    pages = 200,
                ),
            )

            val stopped = dnfBook(
                id = 2,
                pages = 400,
                read = unfinishedRead(
                    id = 2,
                    currentPage = 160,
                ),
            )

            // ----- Act -----
            val result = attributePages(
                updates = updates,
                readBooks = emptyList(),
                didNotFinishBooks = listOf(stopped),
                timeZone = TimeZone.UTC,
            )

            // ----- Assert -----
            result.map { it.kind } shouldBe listOf(AttributedPagesKind.Update)
        }

        @Test
        fun `credits no stop remainder to a stopped book without a stop date`() {
            // ----- Arrange -----
            val stopped = dnfBook(
                id = 2,
                stoppedAt = null,
                pages = 400,
                read = unfinishedRead(
                    id = 2,
                    currentPage = 160,
                ),
            )

            // ----- Act -----
            val result = attributePages(
                updates = emptyList(),
                readBooks = emptyList(),
                didNotFinishBooks = listOf(stopped),
                timeZone = TimeZone.UTC,
            )

            // ----- Assert -----
            result.shouldBeEmpty()
        }

        @Test
        fun `credits no stop remainder when the stop point is unknown`() {
            // ----- Arrange -----
            val stopped = dnfBook(
                id = 2,
                pages = 400,
                read = unfinishedRead(id = 2),
            )

            // ----- Act -----
            val result = attributePages(
                updates = emptyList(),
                readBooks = emptyList(),
                didNotFinishBooks = listOf(stopped),
                timeZone = TimeZone.UTC,
            )

            // ----- Assert -----
            result.shouldBeEmpty()
        }

        @Test
        fun `measures the finish remainder against the readers edition pages over the book pages`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-03-12",
                    pages = 200,
                    editions = listOf(
                        edition(
                            id = 10,
                            pages = 300,
                        ),
                    ),
                    editionId = 10,
                ),
            )

            // ----- Act -----
            val result = attributePages(
                updates = emptyList(),
                readBooks = books,
                didNotFinishBooks = emptyList(),
                timeZone = TimeZone.UTC,
            )

            // ----- Assert -----
            result.map { it.pages } shouldBe listOf(300)
        }
    }

    @Nested
    inner class LastPositionOnOrBefore {
        private val finishDate = LocalDate(
            year = 2026,
            month = 3,
            day = 12,
        )

        @Test
        fun `ignores updates after the date and updates without a page`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 1,
                    at = "2026-03-10T10:00:00Z",
                    pagesRead = 100,
                    pages = 100,
                ),
                update(
                    bookId = 1,
                    at = "2026-03-11T10:00:00Z",
                    pagesRead = 0,
                ),
                update(
                    bookId = 1,
                    at = "2026-03-13T10:00:00Z",
                    pagesRead = 50,
                    pages = 150,
                ),
            )

            // ----- Act -----
            val result = lastPositionOnOrBefore(
                updates = updates,
                date = finishDate,
                timeZone = TimeZone.UTC,
            )

            // ----- Assert -----
            result shouldBe 100
        }

        @Test
        fun `is zero when no update qualifies`() {
            // ----- Arrange -----
            val updates = listOf(
                update(
                    bookId = 1,
                    at = "2026-03-13T10:00:00Z",
                    pagesRead = 50,
                    pages = 50,
                ),
            )

            // ----- Act -----
            val result = lastPositionOnOrBefore(
                updates = updates,
                date = finishDate,
                timeZone = TimeZone.UTC,
            )

            // ----- Assert -----
            result shouldBe 0
        }
    }
}

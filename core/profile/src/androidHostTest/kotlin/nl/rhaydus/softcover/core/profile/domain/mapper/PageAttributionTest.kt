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
                    isRemainder = false,
                ),
                AttributedPages(
                    date = LocalDate(
                        year = 2026,
                        month = 3,
                        day = 12,
                    ),
                    bookId = 1,
                    pages = 200,
                    isRemainder = true,
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
                timeZone = TimeZone.UTC,
            )

            // ----- Assert -----
            result.map { it.isRemainder to it.pages } shouldBe listOf(false to 350)
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

package nl.rhaydus.softcover.core.profile.domain.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.softcover.core.domain.model.Quote
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.ReviewParagraph
import nl.rhaydus.softcover.core.domain.model.ReviewRun
import nl.rhaydus.softcover.core.domain.model.StatsRange
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Instant

class WrittenStatsMapperTest {
    private val utc = TimeZone.UTC
    private val june = StatsRange.Bounded(
        start = LocalDate(
            2024,
            6,
            1,
        ),
        endInclusive = LocalDate(
            2024,
            6,
            30,
        ),
    )

    private fun review(vararg paragraphs: String) = ReviewDocument(
        paragraphs = paragraphs.map { ReviewParagraph(runs = listOf(ReviewRun(it))) },
    )

    private fun reviewed(
        id: Int,
        reviewedAt: String?,
        review: ReviewDocument? = review("one two three"),
        status: BookStatus = BookStatus.Read,
    ): Book {
        val book = readBook(id = id)

        return book.copy(
            userBook = requireNotNull(book.userBook).copy(
                status = status,
                reviewDocument = review,
                reviewedAt = reviewedAt,
            ),
        )
    }

    private fun quote(
        bookId: Int,
        at: String,
        localId: Long = at.hashCode().toLong() + bookId,
    ) = Quote(
        localId = localId,
        remoteId = null,
        bookId = bookId,
        editionId = null,
        text = "q",
        privacy = PrivacySetting.PUBLIC,
        actionAt = Instant.parse(at),
        position = null,
        isPending = false,
    )

    private fun stats(
        books: List<Book> = emptyList(),
        quotes: List<Quote> = emptyList(),
        range: StatsRange = june,
        timeZone: TimeZone = utc,
    ) = toWrittenStats(
        books = books,
        quotes = quotes,
        range = range,
        timeZone = timeZone,
    )

    @Nested
    inner class Reviews {
        @Test
        fun `counts a review on a non-Read shelf when reviewedAt is in range`() {
            // ----- Arrange -----
            val book = reviewed(
                id = 1,
                reviewedAt = "2024-06-10",
                status = BookStatus.Reading,
            )

            // ----- Act -----
            val result = stats(books = listOf(book))

            // ----- Assert -----
            result.reviewCount shouldBe 1
        }

        @Test
        fun `excludes blank and missing reviews`() {
            // ----- Arrange -----
            val books = listOf(
                reviewed(
                    id = 1,
                    reviewedAt = "2024-06-10",
                    review = null,
                ),
                reviewed(
                    id = 2,
                    reviewedAt = "2024-06-10",
                    review = review("  "),
                ),
                reviewed(
                    id = 3,
                    reviewedAt = "2024-06-10",
                    review = ReviewDocument.EMPTY,
                ),
            )

            // ----- Act -----
            val result = stats(books = books)

            // ----- Assert -----
            result.reviewCount shouldBe 0
            result.reviewWordCount shouldBe 0
        }

        @Test
        fun `parses date-only and date-time reviewedAt and excludes dates outside the range`() {
            // ----- Arrange -----
            val books = listOf(
                reviewed(
                    id = 1,
                    reviewedAt = "2024-06-01",
                ),
                reviewed(
                    id = 2,
                    reviewedAt = "2024-06-30T23:59:59",
                ),
                reviewed(
                    id = 3,
                    reviewedAt = "2024-07-01T00:00:00",
                ),
                reviewed(
                    id = 4,
                    reviewedAt = "2024-05-31",
                ),
            )

            // ----- Act -----
            val result = stats(books = books)

            // ----- Assert -----
            result.reviewCount shouldBe 2
        }

        @Test
        fun `a null or unparsable reviewedAt counts only under Unbounded`() {
            // ----- Arrange -----
            val books = listOf(
                reviewed(
                    id = 1,
                    reviewedAt = null,
                ),
                reviewed(
                    id = 2,
                    reviewedAt = "not a date",
                ),
            )

            // ----- Act -----
            val bounded = stats(books = books)

            val unbounded = stats(
                books = books,
                range = StatsRange.Unbounded,
            )

            // ----- Assert -----
            bounded.reviewCount shouldBe 0
            unbounded.reviewCount shouldBe 2
        }

        @Test
        fun `sums words over the counted reviews only`() {
            // ----- Arrange -----
            val books = listOf(
                reviewed(
                    id = 1,
                    reviewedAt = "2024-06-10",
                    review = review(
                        "one two",
                        "three",
                    ),
                ),
                reviewed(
                    id = 2,
                    reviewedAt = "2024-06-11",
                    review = review("four five six seven"),
                ),
                reviewed(
                    id = 3,
                    reviewedAt = "2023-01-01",
                    review = review("ignored words here"),
                ),
            )

            // ----- Act -----
            val result = stats(books = books)

            // ----- Assert -----
            result.reviewCount shouldBe 2
            result.reviewWordCount shouldBe 7
        }

        @Test
        fun `ignores list-only books without a userBook`() {
            // ----- Arrange -----
            val book = readBook(id = 1).copy(userBook = null)

            // ----- Act -----
            val result = stats(
                books = listOf(book),
                range = StatsRange.Unbounded,
            )

            // ----- Assert -----
            result.reviewCount shouldBe 0
        }
    }

    @Nested
    inner class Quotes {
        @Test
        fun `filters quotes by the local date in the given time zone`() {
            // ----- Arrange -----
            // 2024-06-30T22:30Z is already July 1st in Amsterdam (UTC+2) and 2024-06-01T01:00Z is still May 31st in New York.
            val quotes = listOf(
                quote(
                    bookId = 1,
                    at = "2024-06-30T22:30:00Z",
                ),
                quote(
                    bookId = 1,
                    at = "2024-06-15T12:00:00Z",
                ),
                quote(
                    bookId = 1,
                    at = "2024-06-01T01:00:00Z",
                ),
            )

            // ----- Act -----
            val amsterdam = stats(
                quotes = quotes,
                timeZone = TimeZone.of("Europe/Amsterdam"),
            )

            val newYork = stats(
                quotes = quotes,
                timeZone = TimeZone.of("America/New_York"),
            )

            // ----- Assert -----
            amsterdam.quoteCount shouldBe 2
            newYork.quoteCount shouldBe 2
        }

        @Test
        fun `a quote near midnight lands on the local day`() {
            // ----- Arrange -----
            val quotes = listOf(
                quote(
                    bookId = 1,
                    at = "2024-05-31T23:30:00Z",
                ),
            )

            val juneFirst = StatsRange.Bounded(
                LocalDate(
                    2024,
                    6,
                    1,
                ),
                LocalDate(
                    2024,
                    6,
                    1,
                ),
            )

            // ----- Act -----
            val result = stats(
                quotes = quotes,
                range = juneFirst,
                timeZone = TimeZone.of("Europe/Amsterdam"),
            )

            // ----- Assert -----
            result.quoteCount shouldBe 1
        }

        @Test
        fun `quotedBookCount counts distinct books`() {
            // ----- Arrange -----
            val quotes = listOf(
                quote(
                    bookId = 1,
                    at = "2024-06-02T10:00:00Z",
                ),
                quote(
                    bookId = 1,
                    at = "2024-06-03T10:00:00Z",
                ),
                quote(
                    bookId = 2,
                    at = "2024-06-04T10:00:00Z",
                ),
            )

            // ----- Act -----
            val result = stats(quotes = quotes)

            // ----- Assert -----
            result.quoteCount shouldBe 3
            result.quotedBookCount shouldBe 2
        }
    }

    @Nested
    inner class MostQuoted {
        @Test
        fun `picks the book with the most quotes and its earliest quote in range`() {
            // ----- Arrange -----
            val books = listOf(readBook(id = 1), readBook(id = 2))

            val earliest = quote(
                bookId = 2,
                at = "2024-06-02T10:00:00Z",
            )

            val quotes = listOf(
                quote(
                    bookId = 1,
                    at = "2024-06-05T10:00:00Z",
                ),
                quote(
                    bookId = 2,
                    at = "2024-06-20T10:00:00Z",
                ),
                earliest,
                quote(
                    bookId = 2,
                    at = "2024-05-01T10:00:00Z",
                ),
            )

            // ----- Act -----
            val result = stats(
                books = books,
                quotes = quotes,
            ).mostQuoted.shouldNotBeNull()

            // ----- Assert -----
            result.book.id shouldBe 2
            result.quoteCount shouldBe 2
            result.firstQuote shouldBe earliest
        }

        @Test
        fun `a tie goes to the most recently quoted book`() {
            // ----- Arrange -----
            val books = listOf(readBook(id = 1), readBook(id = 2))

            val quotes = listOf(
                quote(
                    bookId = 1,
                    at = "2024-06-02T10:00:00Z",
                ),
                quote(
                    bookId = 1,
                    at = "2024-06-25T10:00:00Z",
                ),
                quote(
                    bookId = 2,
                    at = "2024-06-03T10:00:00Z",
                ),
                quote(
                    bookId = 2,
                    at = "2024-06-10T10:00:00Z",
                ),
            )

            // ----- Act -----
            val result = stats(
                books = books,
                quotes = quotes,
            ).mostQuoted.shouldNotBeNull()

            // ----- Assert -----
            result.book.id shouldBe 1
        }

        @Test
        fun `only shelved books qualify`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(id = 1).copy(userBook = null),
                readBook(id = 2),
            )

            val quotes = listOf(
                quote(
                    bookId = 1,
                    at = "2024-06-02T10:00:00Z",
                ),
                quote(
                    bookId = 1,
                    at = "2024-06-03T10:00:00Z",
                ),
                quote(
                    bookId = 2,
                    at = "2024-06-04T10:00:00Z",
                ),
            )

            // ----- Act -----
            val result = stats(
                books = books,
                quotes = quotes,
            )

            // ----- Assert -----
            result.mostQuoted.shouldNotBeNull().book.id shouldBe 2
            result.quotedBookCount shouldBe 2
        }

        @Test
        fun `is null when no shelved book has a quote in range`() {
            // ----- Arrange -----
            val books = listOf(readBook(id = 1), readBook(id = 2).copy(userBook = null))

            val quotes = listOf(
                quote(
                    bookId = 1,
                    at = "2023-06-02T10:00:00Z",
                ),
                quote(
                    bookId = 2,
                    at = "2024-06-02T10:00:00Z",
                ),
            )

            // ----- Act -----
            val result = stats(
                books = books,
                quotes = quotes,
            )

            // ----- Assert -----
            result.mostQuoted.shouldBeNull()
        }
    }
}

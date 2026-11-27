package nl.rhaydus.softcover.core.profile.domain.mapper

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.Quote
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.domain.model.isBlank
import nl.rhaydus.softcover.core.domain.model.wordCount
import nl.rhaydus.softcover.core.profile.domain.model.MostQuotedBook
import nl.rhaydus.softcover.core.profile.domain.model.WrittenStats

fun toWrittenStats(
    books: List<Book>,
    quotes: List<Quote>,
    range: StatsRange,
    timeZone: TimeZone,
): WrittenStats {
    val reviews: List<ReviewDocument> = books.mapNotNull { it.countedReview(range) }

    val quotesInRange: List<Quote> = quotes.filter { range.contains(it.actionAt.toLocalDateTime(timeZone).date) }

    return WrittenStats(
        reviewCount = reviews.size,
        reviewWordCount = reviews.sumOf { it.wordCount() },
        quoteCount = quotesInRange.size,
        quotedBookCount = quotesInRange.distinctBy { it.bookId }.size,
        mostQuoted = mostQuotedBook(
            books = books,
            quotesInRange = quotesInRange,
        ),
    )
}

// reviewedAt is naive (the optimistic stamp is date-only, the server's carries a time), so the date part
// is read as written, never shifted through a timezone.
private fun Book.countedReview(range: StatsRange): ReviewDocument? {
    val userBook = userBook ?: return null

    val review: ReviewDocument = userBook.reviewDocument?.takeUnless { it.isBlank() } ?: return null

    if (range is StatsRange.Unbounded) return review

    val reviewedOn: LocalDate = userBook.reviewedAt
        ?.substringBefore('T')
        ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        ?: return null

    return review.takeIf { range.contains(reviewedOn) }
}

private fun mostQuotedBook(
    books: List<Book>,
    quotesInRange: List<Quote>,
): MostQuotedBook? {
    val quotesByBook: Map<Int, List<Quote>> = quotesInRange.groupBy { it.bookId }

    return books
        .filter { it.userBook != null }
        .mapNotNull { book -> quotesByBook[book.id]?.let { book to it } }
        .maxWithOrNull(
            compareBy<Pair<Book, List<Quote>>> { (_, bookQuotes) -> bookQuotes.size }
                .thenBy { (_, bookQuotes) -> bookQuotes.maxOf { it.actionAt } },
        )
        ?.let { (book, bookQuotes) ->
            MostQuotedBook(
                book = book,
                quoteCount = bookQuotes.size,
                firstQuote = bookQuotes.minBy { it.actionAt },
            )
        }
}

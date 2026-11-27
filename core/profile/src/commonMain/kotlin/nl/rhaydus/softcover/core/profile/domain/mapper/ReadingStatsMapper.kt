package nl.rhaydus.softcover.core.profile.domain.mapper

import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.core.profile.domain.model.AuthorRanking
import nl.rhaydus.softcover.core.profile.domain.model.FormatSplit
import nl.rhaydus.softcover.core.profile.domain.model.LongestBook
import nl.rhaydus.softcover.core.profile.domain.model.OwnTagCount
import nl.rhaydus.softcover.core.profile.domain.model.RatingBand
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStats
import kotlin.math.roundToInt

private const val RATING_BUCKET_COUNT = 9
private const val RATING_BUCKET_STEP = 0.5
private const val RATING_BUCKET_FLOOR = 1.0
private const val TOP_AUTHOR_COUNT = 5

/**
 * Aggregates the books of [this] that fall in [range]. Under [StatsRange.Bounded] a book without a
 * finish date is left out; under [StatsRange.Unbounded] it counts like any other.
 */
internal fun List<Book>.toReadingStats(
    range: StatsRange,
    userTagsByBookId: Map<Int, List<UserTag>>,
): ReadingStats {
    val inRange = filter { it.isFinishedIn(range) }

    return ReadingStats(
        booksRead = inRange.size,
        genres = inRange.toGenreBreakdown(userTagsByBookId),
        ratings = inRange.toRatingsDistribution(),
        topAuthors = inRange.toTopAuthors(),
        formatSplit = inRange.toFormatSplit(),
        longestBook = inRange.longestBook(),
        ownTags = inRange.toOwnTagCounts(userTagsByBookId),
    )
}

/** The reader's own edition decides the page count, then the book's, since editions differ widely. */
internal fun Book.statsPages(): Int = readerEdition?.pages ?: pages ?: 0

/**
 * The book with the most pages; a tie goes to the later finish (none ranks last), then the higher
 * book id. Null when no book has a known page count.
 */
internal fun List<Book>.longestBook(): LongestBook? {
    val winner = filter { it.statsPages() > 0 }
        .maxWithOrNull(
            compareBy<Book> { it.statsPages() }
                .thenBy(nullsFirst()) { it.userBook?.finishDate }
                .thenBy { it.id },
        ) ?: return null

    return LongestBook(
        book = winner,
        pages = winner.statsPages(),
    )
}

private fun List<Book>.toFormatSplit(): FormatSplit {
    val formats = map { it.readerEdition?.readingFormat }

    return FormatSplit(
        counts = formats.filterNotNull().groupingBy { it }.eachCount(),
        unknownCount = formats.count { it == null },
    )
}

private fun Book.isFinishedIn(range: StatsRange): Boolean = when (range) {
    StatsRange.Unbounded -> true
    is StatsRange.Bounded -> userBook?.finishDate?.let { it in range } == true
}

private fun List<Book>.toRatingsDistribution(): RatingsDistribution {
    val ratings = mapNotNull { it.userBook?.rating }
    val average = if (ratings.isEmpty()) 0.0 else ratings.average()
    val buckets = ratings.toHalfStarBuckets()

    return RatingsDistribution(
        average = average,
        totalRatings = ratings.size,
        halfStarBuckets = buckets,
        band = RatingBand.classify(
            halfStarBuckets = buckets,
            average = average,
            totalRatings = ratings.size,
        ),
    )
}

// The rating scale has ten values (0.5..5.0) but the histogram nine buckets (1.0..5.0), so a 0.5
// rating folds into the 1.0 bucket.
private fun List<Double>.toHalfStarBuckets(): List<Int> {
    val buckets = MutableList(RATING_BUCKET_COUNT) { 0 }

    forEach { rating ->
        val index = ((rating - RATING_BUCKET_FLOOR) / RATING_BUCKET_STEP)
            .roundToInt()
            .coerceIn(
                0,
                RATING_BUCKET_COUNT - 1,
            )

        buckets[index]++
    }

    return buckets
}

// Names are matched exactly, so a row lines up with the library's (category, name) filter key; a name
// repeated on one book counts once. Ties order by lowercase name, then by name, as the library list does.
private fun List<Book>.toOwnTagCounts(userTagsByBookId: Map<Int, List<UserTag>>): List<OwnTagCount> = flatMap { book ->
    userTagsByBookId[book.id].orEmpty()
        .filter { it.category == TagCategory.TAG }
        .map { it.name }
        .distinct()
}.groupingBy { it }
    .eachCount()
    .map { (name, bookCount) ->
        OwnTagCount(
            name = name,
            bookCount = bookCount,
        )
    }
    .sortedWith(
        compareByDescending<OwnTagCount> { it.bookCount }
            .thenBy { it.name.lowercase() }
            .thenBy { it.name },
    )

private fun List<Book>.toTopAuthors(): List<AuthorRanking> {
    val booksByAuthor = flatMap { book -> book.primaryAuthors.map { it to book } }
        .groupBy { it.first.id }

    return booksByAuthor.values
        .map { pairs ->
            val books = pairs.map { it.second }.distinctBy { it.id }
            val ratings = books.mapNotNull { it.userBook?.rating }

            AuthorRanking(
                author = pairs.first().first,
                booksRead = books.size,
                averageRating = if (ratings.isEmpty()) null else ratings.average(),
                leadBook = books
                    .sortedWith(
                        compareBy<Book, LocalDate?>(nullsLast(reverseOrder())) { it.userBook?.finishDate }
                            .thenBy { it.title }
                            .thenBy { it.id },
                    )
                    .first(),
            )
        }
        .sortedWith(
            compareByDescending<AuthorRanking> { it.booksRead }
                .thenBy(nullsLast(reverseOrder())) { it.averageRating }
                .thenBy(nullsLast(reverseOrder())) { it.leadBook.userBook?.finishDate }
                .thenBy { it.author.name },
        )
        .take(TOP_AUTHOR_COUNT)
}

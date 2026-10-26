package nl.rhaydus.softcover.core.profile.domain.mapper

import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.Gender
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.domain.model.Tag
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.core.profile.domain.model.AuthorDemographics
import nl.rhaydus.softcover.core.profile.domain.model.AuthorRanking
import nl.rhaydus.softcover.core.profile.domain.model.DemographicBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenderSlice
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenreSlice
import nl.rhaydus.softcover.core.profile.domain.model.RatingBand
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStats
import kotlin.math.roundToInt

private const val GENRES_PER_BOOK = 10
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
        totalPages = inRange.sumOf { it.statsPages() },
        genres = inRange.toGenreBreakdown(userTagsByBookId),
        ratings = inRange.toRatingsDistribution(),
        authorDemographics = inRange.toAuthorDemographics(),
        topAuthors = inRange.toTopAuthors(),
    )
}

/** The reader's own edition decides the page count, then the book's, since editions differ widely. */
internal fun Book.statsPages(): Int =
    editions.firstOrNull { it.id == userBook?.editionId }?.pages ?: pages ?: 0

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

// Fractions are shares of the books with at least one genre, not of genre assignments; untagged books
// stay out of the denominator. A book counts with its top Hardcover genres plus the reader's own, deduped
// ignoring case so one genre is one slice, and the most common Hardcover spelling (ties by name) wins where both exist.
private fun List<Book>.toGenreBreakdown(userTagsByBookId: Map<Int, List<UserTag>>): GenreBreakdown {
    val genreNamesPerBook = map { book ->
        book.hardcoverGenreNames() + userGenreNames(userTagsByBookId[book.id].orEmpty())
    }.map { names -> names.distinctBy { it.lowercase() } }
        .filter { it.isNotEmpty() }

    if (genreNamesPerBook.isEmpty()) return GenreBreakdown()

    val hardcoverSpellings = flatMap { it.hardcoverGenreNames() }
        .groupBy { it.lowercase() }
        .mapValues { (_, spellings) ->
            spellings
                .groupingBy { it }
                .eachCount()
                .entries
                .sortedWith(
                    compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key },
                )
                .first()
                .key
        }

    val slices = genreNamesPerBook
        .flatten()
        .groupBy { it.lowercase() }
        .map { (key, spellings) ->
            GenreSlice(
                name = hardcoverSpellings[key] ?: spellings.first(),
                count = spellings.size,
                fraction = spellings.size.toDouble() / genreNamesPerBook.size,
            )
        }
        .sortedWith(
            compareByDescending<GenreSlice> { it.count }.thenBy { it.name },
        )

    return GenreBreakdown(slices = slices)
}

private fun Book.hardcoverGenreNames(): List<String> = tags
    .filter { it.category == TagCategory.GENRE }
    .sortedWith(
        compareByDescending<Tag> { it.count }.thenBy { it.name },
    )
    .take(GENRES_PER_BOOK)
    .map { it.name }

private fun userGenreNames(userTags: List<UserTag>): List<String> = userTags
    .filter { it.category == TagCategory.GENRE }
    .map { it.name }

// Authors are deduped by id across the range, so one who wrote many of the books counts once. A
// null gender is Gender.Unknown, a bucket of its own pinned last and never folded into Other. No
// field identifies non-person contributors such as anthology placeholders, so they are counted.
private fun List<Book>.toAuthorDemographics(): AuthorDemographics {
    val authors = flatMap { it.authors }.distinctBy { it.id }
    val totalAuthors = authors.size
    val genderCounts = authors.groupingBy { it.gender }.eachCount()
    val unknownGenderCount = genderCounts[Gender.Unknown] ?: 0

    val knownGenderSlices = genderCounts.entries
        .filter { it.key != Gender.Unknown }
        .sortedByDescending { it.value }
        .map { (gender, count) ->
            GenderSlice(
                gender = gender,
                count = count,
                fraction = count.toDouble() / totalAuthors,
            )
        }
    val genderSlices = if (unknownGenderCount > 0) {
        knownGenderSlices +
            GenderSlice(
                gender = Gender.Unknown,
                count = unknownGenderCount,
                fraction = unknownGenderCount.toDouble() / totalAuthors,
            )
    } else {
        knownGenderSlices
    }

    return AuthorDemographics(
        genderSlices = genderSlices,
        knownGenderCount = totalAuthors - unknownGenderCount,
        unknownGenderCount = unknownGenderCount,
        bipocBreakdown = authors.toDemographicBreakdown { it.isBipoc },
        lgbtqBreakdown = authors.toDemographicBreakdown { it.isLgbtq },
    )
}

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

// Every author lands in exactly one of yes, no or unknown, so the breakdown totals the author count.
private fun List<Author>.toDemographicBreakdown(attribute: (Author) -> Boolean?): DemographicBreakdown {
    val values = map(attribute)

    return DemographicBreakdown(
        yesCount = values.count { it == true },
        noCount = values.count { it == false },
        unknownCount = values.count { it == null },
    )
}

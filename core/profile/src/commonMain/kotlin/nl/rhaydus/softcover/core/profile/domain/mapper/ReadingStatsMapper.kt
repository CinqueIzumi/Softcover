package nl.rhaydus.softcover.core.profile.domain.mapper

import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.Gender
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.profile.domain.model.AuthorDemographics
import nl.rhaydus.softcover.core.profile.domain.model.DemographicBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenderSlice
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenreSlice
import nl.rhaydus.softcover.core.profile.domain.model.RatingBand
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStats
import kotlin.math.roundToInt

private const val TOP_GENRE_COUNT = 5
private const val RATING_BUCKET_COUNT = 9
private const val RATING_BUCKET_STEP = 0.5
private const val RATING_BUCKET_FLOOR = 1.0

/**
 * Aggregates the books of [this] that fall in [range]. Under [StatsRange.Bounded] a book without a
 * finish date is left out; under [StatsRange.Unbounded] it counts like any other.
 */
internal fun List<Book>.toReadingStats(range: StatsRange): ReadingStats {
    val inRange = filter { it.isFinishedIn(range) }

    return ReadingStats(
        booksRead = inRange.size,
        totalPages = inRange.sumOf { it.statsPages() },
        genres = inRange.toGenreBreakdown(),
        ratings = inRange.toRatingsDistribution(),
        authorDemographics = inRange.toAuthorDemographics(),
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

// Fractions are shares of the genre-tagged books, not of genre assignments, and untagged books stay
// out of the denominator; see GenreBreakdown. Names are deduped per book so the count per genre is a
// book count. Only the top genres are kept, with no remainder slice, because genres overlap.
private fun List<Book>.toGenreBreakdown(): GenreBreakdown {
    val genreNamesPerBook = map { book ->
        book.tags
            .filter { it.category == TagCategory.GENRE }
            .map { it.name }
            .distinct()
    }.filter { it.isNotEmpty() }

    if (genreNamesPerBook.isEmpty()) return GenreBreakdown()

    val slices = genreNamesPerBook
        .flatten()
        .groupingBy { it }
        .eachCount()
        .entries
        .sortedByDescending { it.value }
        .take(TOP_GENRE_COUNT)
        .map { (name, count) ->
            GenreSlice(
                name = name,
                count = count,
                fraction = count.toDouble() / genreNamesPerBook.size,
            )
        }

    return GenreBreakdown(
        slices = slices,
        taggedBookCount = genreNamesPerBook.size,
    )
}

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

// Every author lands in exactly one of yes, no or unknown, so the breakdown totals the author count.
private fun List<Author>.toDemographicBreakdown(attribute: (Author) -> Boolean?): DemographicBreakdown {
    val values = map(attribute)

    return DemographicBreakdown(
        yesCount = values.count { it == true },
        noCount = values.count { it == false },
        unknownCount = values.count { it == null },
    )
}

package nl.rhaydus.softcover.core.profile.data.mapper

import kotlin.math.roundToInt
import nl.rhaydus.softcover.core.domain.model.Gender
import nl.rhaydus.softcover.core.profile.data.model.StatsAuthor
import nl.rhaydus.softcover.core.profile.data.model.StatsBook
import nl.rhaydus.softcover.core.profile.domain.model.AuthorDemographics
import nl.rhaydus.softcover.core.profile.domain.model.DemographicBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenderSlice
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenreSlice
import nl.rhaydus.softcover.core.profile.domain.model.MonthCount
import nl.rhaydus.softcover.core.profile.domain.model.YearCount

private const val TOP_GENRE_COUNT = 5
private const val YEAR_CHART_SPAN = 8
private const val MONTHS_IN_YEAR = 12
private const val RATING_BUCKET_COUNT = 9
private const val RATING_BUCKET_STEP = 0.5
private const val RATING_BUCKET_FLOOR = 1.0

// Books with no finish date (neither last_read_date nor a finished journal entry) cannot be
// bucketed by year/month and are silently excluded from these two charts - they still count
// toward totalPagesRead and the ratings/genre breakdowns, which don't need a date.
// Contiguous last-8-years, ascending, zero-filled: always returns exactly 8 YearCount entries for
// (currentYear - 7)..currentYear in ascending order, so the bar chart is a real timeline rather than
// sparse floating bars. Reads outside that window are excluded from the chart (they still count
// toward totalPagesRead and other aggregates, which aren't scoped to this window).
internal fun List<StatsBook>.toBooksByYear(currentYear: Int): List<YearCount> {
    val countsByYear = mutableMapOf<Int, Int>()

    forEach { book ->
        val year = book.finishDate?.year ?: return@forEach
        countsByYear[year] = (countsByYear[year] ?: 0) + 1
    }

    return yearRange(currentYear).map { year ->
        YearCount(
            year = year,
            count = countsByYear[year] ?: 0,
        )
    }
}

// Contiguous last-8-years, ascending, zero-filled - see toBooksByYear for the rationale.
internal fun List<StatsBook>.toPagesByYear(currentYear: Int): List<YearCount> {
    val pagesByYear = mutableMapOf<Int, Int>()

    forEach { book ->
        val year = book.finishDate?.year ?: return@forEach
        pagesByYear[year] = (pagesByYear[year] ?: 0) + book.pages
    }

    return yearRange(currentYear).map { year ->
        YearCount(
            year = year,
            count = pagesByYear[year] ?: 0,
        )
    }
}

private fun yearRange(currentYear: Int): IntRange = (currentYear - YEAR_CHART_SPAN + 1)..currentYear

// Always returns all 12 months of currentYear, zero-filled where there is no data, so the
// ridgeline chart never has to special-case a missing month.
internal fun List<StatsBook>.toPagesByMonth(currentYear: Int): List<MonthCount> {
    val pagesByMonth = mutableMapOf<Int, Int>()

    forEach { book ->
        val date = book.finishDate ?: return@forEach

        if (date.year != currentYear) return@forEach

        @Suppress("DEPRECATION")
        val month = date.monthNumber

        pagesByMonth[month] = (pagesByMonth[month] ?: 0) + book.pages
    }

    return (1..MONTHS_IN_YEAR).map { month ->
        MonthCount(
            year = currentYear,
            month = month,
            count = pagesByMonth[month] ?: 0,
        )
    }
}

internal fun List<StatsBook>.toTotalPages(): Int = sumOf { it.pages }

// Scoped to the same read-books dataset as toRatingHalfStarBuckets, so the histogram bars sum to
// this total and RatingBand's bimodal-fraction math isn't deflated by an all-status denominator.
// The hero "Avg. rating" tile stays on the server's all-status rated_books.avg - this pair is only
// for the ratings-section distribution.
internal fun List<StatsBook>.toRatingAverage(): Double {
    val ratings = mapNotNull { it.rating }

    if (ratings.isEmpty()) return 0.0

    return ratings.average()
}

internal fun List<StatsBook>.toRatedCount(): Int = count { it.rating != null }

// The 0-5, 0.5-step rating scale has 10 possible values (0.5..5.0); this histogram only has 9
// buckets (1.0..5.0), so a 0.5 rating rounds down into the 1.0 bucket rather than getting its own.
internal fun List<StatsBook>.toRatingHalfStarBuckets(): List<Int> {
    val buckets = MutableList(RATING_BUCKET_COUNT) { 0 }

    forEach { book ->
        val rating = book.rating ?: return@forEach
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

// Shares of *books*, not of genre assignments: a genre's fraction is the number of the reader's
// genre-tagged books carrying it over the number of genre-tagged books, so each slice reads directly
// as "N% of your books". Because genreNames is deduped per book in toStatsBook(), eachCount() over
// the flattened names is already a per-genre *book* count - only the denominator distinguishes this
// from a share of assignments.
//
// The denominator was assignments (the flattened size) until 3.1.1. That capped every percentage at
// 1/(average genres per book): a reader whose every book was Fantasy saw Fantasy at ~17%, never
// 100%, and the ceiling sank further the more completely a book was tagged. It also made each number
// depend on how thoroughly other people had tagged the reader's books rather than on their reading.
//
// Untagged books are dropped from the denominator rather than counted, so the percentages describe
// the corpus that actually carries genre data - see GenreBreakdown for why. They contributed nothing
// under the old assignments denominator either (a book with no genres adds no assignments), so this
// is the first time the distinction has been visible at all.
//
// Only the top 5 genres are returned, in descending order; the rest are dropped rather than folded
// into a remainder slice, because overlapping genres have no remainder to compute.
internal fun List<StatsBook>.toGenreBreakdown(): GenreBreakdown {
    val taggedBooks = filter { it.genreNames.isNotEmpty() }

    if (taggedBooks.isEmpty()) return GenreBreakdown()

    val slices = taggedBooks
        .flatMap { it.genreNames }
        .groupingBy { it }
        .eachCount()
        .entries
        .sortedByDescending { it.value }
        .take(TOP_GENRE_COUNT)
        .map { (name, count) ->
            GenreSlice(
                name = name,
                count = count,
                fraction = count.toDouble() / taggedBooks.size,
            )
        }

    return GenreBreakdown(
        slices = slices,
        taggedBookCount = taggedBooks.size,
    )
}

// Lifetime, un-windowed: the number of distinct calendar years a finished book resolves to, across
// the whole stats dataset - unlike toBooksByYear/toPagesByYear this is not clipped to the 8-year
// chart window, so a one-off read from e.g. 2015 still counts. Books with no resolvable finish date
// don't contribute.
internal fun List<StatsBook>.toTrackedYears(): Int = mapNotNull { it.finishDate?.year }.toSet().size

// Authors are deduped by id across the whole dataset before any of these stats are computed, so an
// author who wrote many of the user's finished books is counted once - like toGenreBreakdown, which
// counts each genre once per book, an author identity is a single, non-repeating fact about the person.
//
// Per docs/reference/architecture.md -> Unresolvable API enums: a null gender_id (Gender.Unknown) is
// a distinct bucket that is never folded into a real gender or merged with Other - it surfaces as its
// own labelled segment (pinned last). Known limitation: this gender/BIPOC/LGBTQ+ bucket may include non-person
// contributors (e.g. an anthology or "various" placeholder) that Hardcover models as a taggable
// author but that isn't a real person; there is currently no field to detect and filter those out, so
// v1 computes over all distinct tagged authors as-is.
internal fun List<StatsBook>.toAuthorDemographics(): AuthorDemographics {
    val authors = flatMap { it.authors }.distinctBy { it.id }

    val totalAuthors = authors.size
    val genderCounts = authors
        .groupingBy { it.gender }
        .eachCount()
    val unknownGenderCount = genderCounts[Gender.Unknown] ?: 0
    val knownGenderCount = totalAuthors - unknownGenderCount

    // Known genders ranked by count, then the Unknown bucket pinned last as its own segment. Every
    // fraction is a share of ALL distinct authors (not just the tagged subset), so the slices sum to 1.
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
        knownGenderCount = knownGenderCount,
        unknownGenderCount = unknownGenderCount,
        bipocBreakdown = authors.toDemographicBreakdown { it.isBipoc },
        lgbtqBreakdown = authors.toDemographicBreakdown { it.isLgbtq },
    )
}

// Every distinct author contributes to exactly one bucket - yes, no, or unknown - so total() over
// the result always equals the full distinct-author count, not just the tagged subset. An
// all-unknown population is a valid, meaningful DemographicBreakdown(0, 0, N) rather than absent.
private fun List<StatsAuthor>.toDemographicBreakdown(
    attribute: (StatsAuthor) -> Boolean?,
): DemographicBreakdown {
    val values = map(attribute)

    return DemographicBreakdown(
        yesCount = values.count { it == true },
        noCount = values.count { it == false },
        unknownCount = values.count { it == null },
    )
}

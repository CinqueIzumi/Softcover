package nl.rhaydus.softcover.core.profile.data.mapper

import kotlin.math.roundToInt
import nl.rhaydus.softcover.core.domain.model.Gender
import nl.rhaydus.softcover.core.profile.data.model.StatsAuthor
import nl.rhaydus.softcover.core.profile.data.model.StatsBook
import nl.rhaydus.softcover.core.profile.domain.model.AuthorDemographics
import nl.rhaydus.softcover.core.profile.domain.model.DemographicBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenderSlice
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

// Each book contributes at most once to any single genre it carries, but a book with several
// genre tags contributes to each of them - so the fractions are shares of genre *assignments*, not
// of books, and they do not sum to 1 across the slices shown. Only the top 5 genres by assignment
// count are returned, in descending order; genres outside the top 5 are dropped rather than folded
// into a remainder slice, because an "everything else" share computed over overlapping assignments
// invites a whole-of-library reading the number doesn't support. genreNames is already deduped per
// book in toStatsBook().
internal fun List<StatsBook>.toGenreSlices(): List<GenreSlice> {
    val genreAssignments = flatMap { it.genreNames }
    val total = genreAssignments.size

    if (total == 0) return emptyList()

    return genreAssignments
        .groupingBy { it }
        .eachCount()
        .entries
        .sortedByDescending { it.value }
        .take(TOP_GENRE_COUNT)
        .map { (name, count) ->
            GenreSlice(
                name = name,
                count = count,
                fraction = count.toDouble() / total,
            )
        }
}

// Lifetime, un-windowed: the number of distinct calendar years a finished book resolves to, across
// the whole stats dataset - unlike toBooksByYear/toPagesByYear this is not clipped to the 8-year
// chart window, so a one-off read from e.g. 2015 still counts. Books with no resolvable finish date
// don't contribute.
internal fun List<StatsBook>.toTrackedYears(): Int = mapNotNull { it.finishDate?.year }.toSet().size

// Authors are deduped by id across the whole dataset before any of these stats are computed, so an
// author who wrote many of the user's finished books is counted once - unlike toGenreSlices, which
// counts per-book assignments, an author identity is a single, non-repeating fact about the person.
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

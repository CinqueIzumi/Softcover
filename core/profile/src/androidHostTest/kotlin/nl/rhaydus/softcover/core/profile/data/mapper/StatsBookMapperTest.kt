package nl.rhaydus.softcover.core.profile.data.mapper

import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.Gender
import nl.rhaydus.softcover.core.profile.data.model.StatsAuthor
import nl.rhaydus.softcover.core.profile.data.model.StatsBook
import nl.rhaydus.softcover.core.profile.domain.model.DemographicBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenderSlice
import nl.rhaydus.softcover.core.profile.domain.model.MonthCount
import nl.rhaydus.softcover.core.profile.domain.model.YearCount
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

private const val EVERYTHING_ELSE = "Everything else"

class StatsBookMapperTest {
    private fun book(
        rating: Double? = null,
        finishDate: LocalDate? = null,
        pages: Int = 0,
        genreNames: List<String> = emptyList(),
        authors: List<StatsAuthor> = emptyList(),
    ): StatsBook = StatsBook(
        rating = rating,
        finishDate = finishDate,
        pages = pages,
        genreNames = genreNames,
        authors = authors,
    )

    private fun statsAuthor(
        id: Int,
        isBipoc: Boolean? = null,
        isLgbtq: Boolean? = null,
        gender: Gender = Gender.Unknown,
    ): StatsAuthor = StatsAuthor(
        id = id,
        isBipoc = isBipoc,
        isLgbtq = isLgbtq,
        gender = gender,
    )

    @Nested
    inner class ToBooksByYear {
        @Test
        fun `empty list returns 8 zero-filled entries for the trailing window`() {
            // ----- Act -----
            val result = emptyList<StatsBook>().toBooksByYear(currentYear = 2026)

            // ----- Assert -----
            result shouldBe (2019..2026).map {
                YearCount(
                    year = it,
                    count = 0,
                )
            }
        }

        @Test
        fun `groups and counts books by finish-date year, zero-filled across the window, ascending`() {
            // ----- Arrange -----
            val books = listOf(
                book(finishDate = LocalDate(
                    2026,
                    3,
                    10,
                ),),
                book(finishDate = LocalDate(
                    2026,
                    7,
                    1,
                ),),
                book(finishDate = LocalDate(
                    2025,
                    1,
                    1,
                ),),
            )

            // ----- Act -----
            val result = books.toBooksByYear(currentYear = 2026)

            // ----- Assert -----
            result shouldBe (2019..2026).map { year ->
                YearCount(
                    year = year,
                    count = when (year) {
                        2025 -> 1
                        2026 -> 2
                        else -> 0
                    },
                )
            }
        }

        @Test
        fun `books with no finish date are excluded`() {
            // ----- Arrange -----
            val books = listOf(book(finishDate = LocalDate(
                2026,
                3,
                10,
            ),), book(finishDate = null),)

            // ----- Act -----
            val result = books.toBooksByYear(currentYear = 2026)

            // ----- Assert -----
            result shouldBe (2019..2026).map { year ->
                YearCount(
                    year = year,
                    count = if (year == 2026) 1 else 0,
                )
            }
        }

        @Test
        fun `a book whose finish year falls outside the window is dropped`() {
            // ----- Arrange -----
            val books = listOf(
                book(finishDate = LocalDate(
                    2010,
                    1,
                    1,
                ),),
                book(finishDate = LocalDate(
                    2026,
                    3,
                    10,
                ),),
            )

            // ----- Act -----
            val result = books.toBooksByYear(currentYear = 2026)

            // ----- Assert -----
            result.sumOf { it.count } shouldBe 1
            result.none { it.year == 2010 } shouldBe true
        }
    }

    @Nested
    inner class ToPagesByYear {
        @Test
        fun `empty list returns 8 zero-filled entries for the trailing window`() {
            // ----- Act -----
            val result = emptyList<StatsBook>().toPagesByYear(currentYear = 2026)

            // ----- Assert -----
            result shouldBe (2019..2026).map {
                YearCount(
                    year = it,
                    count = 0,
                )
            }
        }

        @Test
        fun `sums pages per finish-date year, zero-filled across the window, ascending`() {
            // ----- Arrange -----
            val books = listOf(
                book(
                    finishDate = LocalDate(
                        2026,
                        3,
                        10,
                    ),
                    pages = 300,
                ),
                book(
                    finishDate = LocalDate(
                        2026,
                        7,
                        1,
                    ),
                    pages = 200,
                ),
                book(
                    finishDate = LocalDate(
                        2025,
                        1,
                        1,
                    ),
                    pages = 100,
                ),
            )

            // ----- Act -----
            val result = books.toPagesByYear(currentYear = 2026)

            // ----- Assert -----
            result shouldBe (2019..2026).map { year ->
                YearCount(
                    year = year,
                    count = when (year) {
                        2025 -> 100
                        2026 -> 500
                        else -> 0
                    },
                )
            }
        }

        @Test
        fun `books with no finish date are excluded from the sum`() {
            // ----- Arrange -----
            val books = listOf(book(
                finishDate = LocalDate(
                    2026,
                    3,
                    10,
                ),
                pages = 300,
            ), book(
                finishDate = null,
                pages = 999,
            ),)

            // ----- Act -----
            val result = books.toPagesByYear(currentYear = 2026)

            // ----- Assert -----
            result shouldBe (2019..2026).map { year ->
                YearCount(
                    year = year,
                    count = if (year == 2026) 300 else 0,
                )
            }
        }

        @Test
        fun `a book whose finish year falls outside the window is dropped from the sum`() {
            // ----- Arrange -----
            val books = listOf(
                book(
                    finishDate = LocalDate(
                        2010,
                        1,
                        1,
                    ),
                    pages = 999,
                ),
                book(
                    finishDate = LocalDate(
                        2026,
                        3,
                        10,
                    ),
                    pages = 300,
                ),
            )

            // ----- Act -----
            val result = books.toPagesByYear(currentYear = 2026)

            // ----- Assert -----
            result.sumOf { it.count } shouldBe 300
            result.none { it.year == 2010 } shouldBe true
        }
    }

    @Nested
    inner class ToPagesByMonth {
        @Test
        fun `returns all 12 months zero-filled when the list is empty`() {
            // ----- Act -----
            val result = emptyList<StatsBook>().toPagesByMonth(currentYear = 2026)

            // ----- Assert -----
            result.size shouldBe 12
            result.all { it.count == 0 } shouldBe true
            result.map { it.month } shouldBe (1..12).toList()
            result.all { it.year == 2026 } shouldBe true
        }

        @Test
        fun `sums pages per month, only for books finished in the requested year`() {
            // ----- Arrange -----
            val books = listOf(
                book(
                    finishDate = LocalDate(
                        2026,
                        3,
                        10,
                    ),
                    pages = 300,
                ),
                book(
                    finishDate = LocalDate(
                        2026,
                        3,
                        20,
                    ),
                    pages = 50,
                ),
                book(
                    finishDate = LocalDate(
                        2025,
                        3,
                        1,
                    ),
                    pages = 999,
                ),
            )

            // ----- Act -----
            val result = books.toPagesByMonth(currentYear = 2026)

            // ----- Assert -----
            result.first { it.month == 3 } shouldBe MonthCount(
                year = 2026,
                month = 3,
                count = 350,
            )
            result.filterNot { it.month == 3 }.all { it.count == 0 } shouldBe true
        }

        @Test
        fun `books with no finish date are excluded`() {
            // ----- Arrange -----
            val books = listOf(book(
                finishDate = null,
                pages = 999,
            ),)

            // ----- Act -----
            val result = books.toPagesByMonth(currentYear = 2026)

            // ----- Assert -----
            result.all { it.count == 0 } shouldBe true
        }
    }

    @Nested
    inner class ToTotalPages {
        @Test
        fun `empty list returns 0`() {
            // ----- Act & Assert -----
            emptyList<StatsBook>().toTotalPages() shouldBe 0
        }

        @Test
        fun `sums pages across all books regardless of finish date`() {
            // ----- Arrange -----
            val books = listOf(
                book(
                    finishDate = LocalDate(
                        2026,
                        3,
                        10,
                    ),
                    pages = 300,
                ),
                book(
                    finishDate = null,
                    pages = 150,
                ),
            )

            // ----- Act & Assert -----
            books.toTotalPages() shouldBe 450
        }
    }

    @Nested
    inner class ToRatingAverage {
        @Test
        fun `empty list returns 0_0`() {
            // ----- Act & Assert -----
            emptyList<StatsBook>().toRatingAverage() shouldBe 0.0
        }

        @Test
        fun `averages only the non-null ratings, ignoring unrated books`() {
            // ----- Arrange -----
            val books = listOf(book(rating = 4.0), book(rating = null), book(rating = 2.0))

            // ----- Act & Assert -----
            books.toRatingAverage() shouldBe (3.0 plusOrMinus 0.0001)
        }

        @Test
        fun `all books unrated returns 0_0`() {
            // ----- Arrange -----
            val books = listOf(book(rating = null), book(rating = null))

            // ----- Act & Assert -----
            books.toRatingAverage() shouldBe 0.0
        }
    }

    @Nested
    inner class ToRatedCount {
        @Test
        fun `empty list returns 0`() {
            // ----- Act & Assert -----
            emptyList<StatsBook>().toRatedCount() shouldBe 0
        }

        @Test
        fun `counts only books with a non-null rating`() {
            // ----- Arrange -----
            val books = listOf(book(rating = 4.0), book(rating = null), book(rating = 2.0))

            // ----- Act & Assert -----
            books.toRatedCount() shouldBe 2
        }
    }

    @Nested
    inner class ToRatingHalfStarBuckets {
        @Test
        fun `empty list returns 9 zero buckets`() {
            // ----- Act -----
            val result = emptyList<StatsBook>().toRatingHalfStarBuckets()

            // ----- Assert -----
            result shouldBe List(9) { 0 }
        }

        @Test
        fun `a 1_0 rating lands in the first bucket`() {
            // ----- Act -----
            val result = listOf(book(rating = 1.0)).toRatingHalfStarBuckets()

            // ----- Assert -----
            result shouldBe listOf(1, 0, 0, 0, 0, 0, 0, 0, 0)
        }

        @Test
        fun `a 5_0 rating lands in the last bucket`() {
            // ----- Act -----
            val result = listOf(book(rating = 5.0)).toRatingHalfStarBuckets()

            // ----- Assert -----
            result shouldBe listOf(0, 0, 0, 0, 0, 0, 0, 0, 1)
        }

        @Test
        fun `a 4_5 rating lands in the second-to-last bucket`() {
            // ----- Act -----
            val result = listOf(book(rating = 4.5)).toRatingHalfStarBuckets()

            // ----- Assert -----
            result shouldBe listOf(0, 0, 0, 0, 0, 0, 0, 1, 0)
        }

        @Test
        fun `a 0_5 rating folds down into the 1_0 bucket, since there is no dedicated bucket for it`() {
            // ----- Act -----
            val result = listOf(book(rating = 0.5)).toRatingHalfStarBuckets()

            // ----- Assert -----
            result shouldBe listOf(1, 0, 0, 0, 0, 0, 0, 0, 0)
        }

        @Test
        fun `unrated books are not counted in any bucket`() {
            // ----- Act -----
            val result = listOf(book(rating = null)).toRatingHalfStarBuckets()

            // ----- Assert -----
            result shouldBe List(9) { 0 }
        }

        @Test
        fun `multiple ratings accumulate into their respective buckets`() {
            // ----- Arrange -----
            val books = listOf(book(rating = 3.0), book(rating = 3.0), book(rating = 5.0))

            // ----- Act -----
            val result = books.toRatingHalfStarBuckets()

            // ----- Assert -----
            result shouldBe listOf(0, 0, 0, 0, 2, 0, 0, 0, 1)
        }
    }

    @Nested
    inner class ToGenreSlices {
        @Test
        fun `empty list returns an empty list`() {
            // ----- Act & Assert -----
            emptyList<StatsBook>().toGenreSlices() shouldBe emptyList()
        }

        @Test
        fun `books with no genre tags contribute nothing`() {
            // ----- Act & Assert -----
            listOf(book(genreNames = emptyList())).toGenreSlices() shouldBe emptyList()
        }

        @Test
        fun `a book contributes once to each of its distinct genre tags`() {
            // ----- Arrange -----
            val books = listOf(book(genreNames = listOf("Fantasy", "Sci-Fi")))

            // ----- Act -----
            val result = books.toGenreSlices()

            // ----- Assert -----
            result.map { it.name to it.count }.toSet() shouldBe setOf("Fantasy" to 1, "Sci-Fi" to 1)
            result.map { it.fraction }.sum() shouldBe (1.0 plusOrMinus 0.0001)
        }

        @Test
        fun `exactly 5 distinct genres produces no Everything-else slice`() {
            // ----- Arrange -----
            val books = listOf(
                book(genreNames = listOf("A")),
                book(genreNames = listOf("B")),
                book(genreNames = listOf("C")),
                book(genreNames = listOf("D")),
                book(genreNames = listOf("E")),
            )

            // ----- Act -----
            val result = books.toGenreSlices()

            // ----- Assert -----
            result.size shouldBe 5
            result.none { it.name == EVERYTHING_ELSE } shouldBe true
        }

        @Test
        fun `more than 5 genres keeps only the top 5 by assignment count, dropping the rest`() {
            // ----- Arrange -----
            // Assignment counts: Fantasy 10, Mystery 9, Sci-Fi 8, Romance 7, Horror 6, Thriller 5,
            // Comedy 4, Drama 3 - total 52 assignments across a single book's genreNames list.
            val genreNames = List(10) { "Fantasy" } +
                List(9) { "Mystery" } +
                List(8) { "Sci-Fi" } +
                List(7) { "Romance" } +
                List(6) { "Horror" } +
                List(5) { "Thriller" } +
                List(4) { "Comedy" } +
                List(3) { "Drama" }

            // ----- Act -----
            val result = listOf(book(genreNames = genreNames)).toGenreSlices()

            // ----- Assert -----
            result.size shouldBe 5
            result.map { it.name } shouldBe listOf("Fantasy", "Mystery", "Sci-Fi", "Romance", "Horror")
            result.none { it.name == EVERYTHING_ELSE } shouldBe true
            result[0].fraction shouldBe (10.0 / 52.0 plusOrMinus 0.0001)
            result[1].fraction shouldBe (9.0 / 52.0 plusOrMinus 0.0001)
            result[2].fraction shouldBe (8.0 / 52.0 plusOrMinus 0.0001)
            result[3].fraction shouldBe (7.0 / 52.0 plusOrMinus 0.0001)
            result[4].fraction shouldBe (6.0 / 52.0 plusOrMinus 0.0001)
            result.map { it.fraction }.sum() shouldBe (40.0 / 52.0 plusOrMinus 0.0001)
        }
    }

    @Nested
    inner class ToTrackedYears {
        @Test
        fun `empty list returns 0`() {
            // ----- Act & Assert -----
            emptyList<StatsBook>().toTrackedYears() shouldBe 0
        }

        @Test
        fun `two books finished in the same year count as a single distinct year`() {
            // ----- Arrange -----
            val books = listOf(
                book(finishDate = LocalDate(
                    2026,
                    3,
                    10,
                ),),
                book(finishDate = LocalDate(
                    2026,
                    7,
                    1,
                ),),
            )

            // ----- Act & Assert -----
            books.toTrackedYears() shouldBe 1
        }

        @Test
        fun `a book finished well outside the 8-year chart window still counts, proving the result is not clipped to the window`() {
            // ----- Arrange -----
            val books = listOf(
                book(finishDate = LocalDate(
                    2015,
                    1,
                    1,
                ),),
                book(finishDate = LocalDate(
                    2026,
                    3,
                    10,
                ),),
            )

            // ----- Act & Assert -----
            books.toTrackedYears() shouldBe 2
        }

        @Test
        fun `books with no finish date are excluded from the count`() {
            // ----- Arrange -----
            val books = listOf(
                book(finishDate = LocalDate(
                    2026,
                    3,
                    10,
                ),),
                book(finishDate = null),
            )

            // ----- Act & Assert -----
            books.toTrackedYears() shouldBe 1
        }
    }

    @Nested
    inner class ToAuthorDemographics {
        @Test
        fun `empty book list returns empty gender slices, zero counts and empty breakdowns`() {
            // ----- Act -----
            val result = emptyList<StatsBook>().toAuthorDemographics()

            // ----- Assert -----
            result.genderSlices shouldBe emptyList()
            result.knownGenderCount shouldBe 0
            result.unknownGenderCount shouldBe 0
            result.bipocBreakdown shouldBe DemographicBreakdown()
            result.lgbtqBreakdown shouldBe DemographicBreakdown()
        }

        @Test
        fun `books with no authors produce empty gender slices, zero counts and empty breakdowns`() {
            // ----- Arrange -----
            val books = listOf(book(authors = emptyList()), book(authors = emptyList()))

            // ----- Act -----
            val result = books.toAuthorDemographics()

            // ----- Assert -----
            result.genderSlices shouldBe emptyList()
            result.knownGenderCount shouldBe 0
            result.unknownGenderCount shouldBe 0
            result.bipocBreakdown shouldBe DemographicBreakdown()
            result.lgbtqBreakdown shouldBe DemographicBreakdown()
        }

        @Test
        fun `an author appearing across 3 books is counted only once in the gender and demographic breakdowns`() {
            // ----- Arrange -----
            val recurringAuthor = statsAuthor(
                id = 1,
                isBipoc = true,
                isLgbtq = false,
                gender = Gender.Male,
            )
            val books = listOf(
                book(authors = listOf(recurringAuthor)),
                book(authors = listOf(recurringAuthor)),
                book(authors = listOf(recurringAuthor)),
            )

            // ----- Act -----
            val result = books.toAuthorDemographics()

            // ----- Assert -----
            result.knownGenderCount shouldBe 1
            result.unknownGenderCount shouldBe 0
            result.genderSlices shouldBe listOf(
                GenderSlice(
                    gender = Gender.Male,
                    count = 1,
                    fraction = 1.0,
                ),
            )
            result.bipocBreakdown shouldBe DemographicBreakdown(
                yesCount = 1,
                noCount = 0,
                unknownCount = 0,
            )
            result.bipocBreakdown.total shouldBe 1
            result.lgbtqBreakdown shouldBe DemographicBreakdown(
                yesCount = 0,
                noCount = 1,
                unknownCount = 0,
            )
            result.lgbtqBreakdown.total shouldBe 1
        }

        @Test
        fun `an author id repeated with different attributes across books still resolves to a single entry`() {
            // ----- Arrange -----
            // distinctBy keeps only the first occurrence encountered for a given id - the second
            // book's conflicting attributes for the same id must not contribute a second entry.
            val books = listOf(
                book(authors = listOf(statsAuthor(
                    id = 1,
                    gender = Gender.Male,
                ),),),
                book(authors = listOf(statsAuthor(
                    id = 1,
                    gender = Gender.Female,
                ),),),
            )

            // ----- Act -----
            val result = books.toAuthorDemographics()

            // ----- Assert -----
            result.knownGenderCount shouldBe 1
            result.genderSlices shouldBe listOf(
                GenderSlice(
                    gender = Gender.Male,
                    count = 1,
                    fraction = 1.0,
                ),
            )
        }

        @Test
        fun `Gender_Unknown is included in genderSlices as the last element, with fraction over ALL distinct authors`() {
            // ----- Arrange -----
            val books = listOf(
                book(authors = listOf(
                    statsAuthor(
                        id = 1,
                        gender = Gender.Male,
                    ),
                    statsAuthor(
                        id = 2,
                        gender = Gender.Unknown,
                    ),
                    statsAuthor(
                        id = 3,
                        gender = Gender.Unknown,
                    ),
                ),),
            )

            // ----- Act -----
            val result = books.toAuthorDemographics()

            // ----- Assert -----
            result.knownGenderCount shouldBe 1
            result.unknownGenderCount shouldBe 2
            result.genderSlices shouldBe listOf(
                GenderSlice(
                    gender = Gender.Male,
                    count = 1,
                    fraction = 1.0 / 3.0,
                ),
                GenderSlice(
                    gender = Gender.Unknown,
                    count = 2,
                    fraction = 2.0 / 3.0,
                ),
            )
            result.genderSlices.last().gender shouldBe Gender.Unknown
        }

        @Test
        fun `Gender_Other is counted as its own slice and never merged with Unknown`() {
            // ----- Arrange -----
            val books = listOf(
                book(authors = listOf(
                    statsAuthor(
                        id = 1,
                        gender = Gender.Other,
                    ),
                    statsAuthor(
                        id = 2,
                        gender = Gender.Unknown,
                    ),
                ),),
            )

            // ----- Act -----
            val result = books.toAuthorDemographics()

            // ----- Assert -----
            result.knownGenderCount shouldBe 1
            result.unknownGenderCount shouldBe 1
            result.genderSlices shouldBe listOf(
                GenderSlice(
                    gender = Gender.Other,
                    count = 1,
                    fraction = 0.5,
                ),
                GenderSlice(
                    gender = Gender.Unknown,
                    count = 1,
                    fraction = 0.5,
                ),
            )
        }

        @Test
        fun `gender slices are sorted count-descending with Unknown pinned last, and fractions sum to 1_0 over ALL authors`() {
            // ----- Arrange -----
            // Male x3, Female x1, Other x1, Unknown x2 - 7 distinct authors, 5 with a known gender, 2 unknown.
            val books = listOf(
                book(authors = listOf(
                    statsAuthor(
                        id = 1,
                        gender = Gender.Male,
                    ),
                    statsAuthor(
                        id = 2,
                        gender = Gender.Male,
                    ),
                    statsAuthor(
                        id = 3,
                        gender = Gender.Male,
                    ),
                    statsAuthor(
                        id = 4,
                        gender = Gender.Female,
                    ),
                    statsAuthor(
                        id = 5,
                        gender = Gender.Other,
                    ),
                    statsAuthor(
                        id = 6,
                        gender = Gender.Unknown,
                    ),
                    statsAuthor(
                        id = 7,
                        gender = Gender.Unknown,
                    ),
                ),),
            )

            // ----- Act -----
            val result = books.toAuthorDemographics()

            // ----- Assert -----
            result.knownGenderCount shouldBe 5
            result.unknownGenderCount shouldBe 2
            result.genderSlices.first() shouldBe GenderSlice(
                gender = Gender.Male,
                count = 3,
                fraction = 3.0 / 7.0,
            )
            result.genderSlices.last() shouldBe GenderSlice(
                gender = Gender.Unknown,
                count = 2,
                fraction = 2.0 / 7.0,
            )
            result.genderSlices.drop(1).dropLast(1).map { it.gender to it.count }.toSet() shouldBe setOf(
                Gender.Female to 1,
                Gender.Other to 1,
            )
            result.genderSlices.sumOf { it.fraction } shouldBe (1.0 plusOrMinus 0.0001)
        }

        @Test
        fun `Unknown is pinned last even when it is the largest bucket, not sorted to the front by count`() {
            // ----- Arrange -----
            // Male x1, Unknown x5 - 6 distinct authors, 1 with a known gender, 5 unknown.
            val books = listOf(
                book(authors = listOf(
                    statsAuthor(
                        id = 1,
                        gender = Gender.Male,
                    ),
                    statsAuthor(
                        id = 2,
                        gender = Gender.Unknown,
                    ),
                    statsAuthor(
                        id = 3,
                        gender = Gender.Unknown,
                    ),
                    statsAuthor(
                        id = 4,
                        gender = Gender.Unknown,
                    ),
                    statsAuthor(
                        id = 5,
                        gender = Gender.Unknown,
                    ),
                    statsAuthor(
                        id = 6,
                        gender = Gender.Unknown,
                    ),
                ),),
            )

            // ----- Act -----
            val result = books.toAuthorDemographics()

            // ----- Assert -----
            result.knownGenderCount shouldBe 1
            result.unknownGenderCount shouldBe 5
            result.genderSlices shouldBe listOf(
                GenderSlice(
                    gender = Gender.Male,
                    count = 1,
                    fraction = 1.0 / 6.0,
                ),
                GenderSlice(
                    gender = Gender.Unknown,
                    count = 5,
                    fraction = 5.0 / 6.0,
                ),
            )
            result.genderSlices.last().gender shouldBe Gender.Unknown
        }

        @Test
        fun `bipocBreakdown and lgbtqBreakdown are computed independently over distinct authors, each landing in a different one of the yes-no-unknown buckets`() {
            // ----- Arrange -----
            // Author 1: bipoc=true (yes), lgbtq=null (unknown)
            // Author 2: bipoc=false (no), lgbtq=true (yes)
            // Author 3: bipoc=null (unknown), lgbtq=false (no)
            // Every bucket of both breakdowns is exercised, and each breakdown's total equals the
            // full 3-author population, not just the subset with the attribute known.
            val books = listOf(
                book(authors = listOf(
                    statsAuthor(
                        id = 1,
                        isBipoc = true,
                        isLgbtq = null,
                    ),
                    statsAuthor(
                        id = 2,
                        isBipoc = false,
                        isLgbtq = true,
                    ),
                    statsAuthor(
                        id = 3,
                        isBipoc = null,
                        isLgbtq = false,
                    ),
                ),),
            )

            // ----- Act -----
            val result = books.toAuthorDemographics()

            // ----- Assert -----
            result.bipocBreakdown shouldBe DemographicBreakdown(
                yesCount = 1,
                noCount = 1,
                unknownCount = 1,
            )
            result.bipocBreakdown.total shouldBe 3
            result.lgbtqBreakdown shouldBe DemographicBreakdown(
                yesCount = 1,
                noCount = 1,
                unknownCount = 1,
            )
            result.lgbtqBreakdown.total shouldBe 3
        }

        @Test
        fun `bipocBreakdown is all-unknown when every distinct author has a null isBipoc, not absent`() {
            // ----- Arrange -----
            val books = listOf(
                book(authors = listOf(
                    statsAuthor(
                        id = 1,
                        isBipoc = null,
                    ),
                    statsAuthor(
                        id = 2,
                        isBipoc = null,
                    ),
                ),),
            )

            // ----- Act -----
            val result = books.toAuthorDemographics()

            // ----- Assert -----
            result.bipocBreakdown shouldBe DemographicBreakdown(
                yesCount = 0,
                noCount = 0,
                unknownCount = 2,
            )
            result.bipocBreakdown.total shouldBe 2
        }

        @Test
        fun `lgbtqBreakdown and bipocBreakdown are both the empty default when there are no distinct authors at all`() {
            // ----- Act -----
            val result = emptyList<StatsBook>().toAuthorDemographics()

            // ----- Assert -----
            result.lgbtqBreakdown shouldBe DemographicBreakdown()
            result.bipocBreakdown shouldBe DemographicBreakdown()
        }
    }
}

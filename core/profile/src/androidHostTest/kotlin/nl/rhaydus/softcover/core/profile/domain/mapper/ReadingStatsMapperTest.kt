package nl.rhaydus.softcover.core.profile.domain.mapper

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.Gender
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.domain.model.Tag
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.core.profile.domain.model.DemographicBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenreSlice
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadingStatsMapperTest {
    private val bounded = StatsRange.Bounded(
        start = LocalDate(
            2026,
            1,
            1,
        ),
        endInclusive = LocalDate(
            2026,
            12,
            31,
        ),
    )

    private fun genre(
        name: String,
        count: Int = 0,
    ) = Tag(
        id = name.hashCode(),
        name = name,
        category = TagCategory.GENRE,
        count = count,
    )

    private fun userTag(
        name: String,
        category: TagCategory = TagCategory.GENRE,
    ) = UserTag(
        name = name,
        category = category,
    )

    private fun List<Book>.toStats(
        range: StatsRange,
        userTagsByBookId: Map<Int, List<UserTag>> = emptyMap(),
    ) = toReadingStats(
        range = range,
        userTagsByBookId = userTagsByBookId,
    )

    @Nested
    inner class RangeFiltering {
        @Test
        fun `bounded range keeps books finished on either edge and drops those just outside`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2025-12-31",
                ),
                readBook(
                    id = 2,
                    finishedAt = "2026-01-01",
                ),
                readBook(
                    id = 3,
                    finishedAt = "2026-12-31",
                ),
                readBook(
                    id = 4,
                    finishedAt = "2027-01-01",
                ),
            )

            // ----- Act -----
            val stats = books.toStats(bounded)

            // ----- Assert -----
            stats.booksRead shouldBe 2
        }

        @Test
        fun `undated books count only under an unbounded range`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-05-01",
                ),
                readBook(id = 2),
            )

            // ----- Act -----
            val boundedStats = books.toStats(bounded)
            val unboundedStats = books.toStats(StatsRange.Unbounded)

            // ----- Assert -----
            boundedStats.booksRead shouldBe 1
            unboundedStats.booksRead shouldBe 2
        }

        @Test
        fun `a re-read counts once, in the year of its latest finish`() {
            // ----- Arrange -----
            val reread = readBook(
                id = 1,
                reads = listOf(
                    finishedRead(
                        id = 1,
                        finishedAt = "2025-03-01",
                    ),
                    finishedRead(
                        id = 2,
                        finishedAt = "2026-03-01",
                    ),
                ),
            )
            val previousYear = StatsRange.Bounded(
                LocalDate(
                    2025,
                    1,
                    1,
                ),
                LocalDate(
                    2025,
                    12,
                    31,
                ),
            )

            // ----- Act -----
            val latestYearStats = listOf(reread).toStats(bounded)
            val previousYearStats = listOf(reread).toStats(previousYear)

            // ----- Assert -----
            latestYearStats.booksRead shouldBe 1
            previousYearStats.booksRead shouldBe 0
        }

        @Test
        fun `no books yields zeroed stats`() {
            // ----- Arrange -----
            val books = emptyList<Book>()

            // ----- Act -----
            val stats = books.toStats(StatsRange.Unbounded)

            // ----- Assert -----
            stats.booksRead shouldBe 0
            stats.totalPages shouldBe 0
            stats.genres.slices shouldBe emptyList()
            stats.ratings.totalRatings shouldBe 0
            stats.ratings.average shouldBe 0.0
        }
    }

    @Nested
    inner class Pages {
        @Test
        fun `the readers edition pages win over the book pages`() {
            // ----- Arrange -----
            val book = readBook(
                id = 1,
                finishedAt = "2026-02-01",
                pages = 200,
                editions = listOf(
                    edition(
                        id = 10,
                        pages = 300,
                    ),
                ),
                editionId = 10,
            )

            // ----- Act -----
            val stats = listOf(book).toStats(StatsRange.Unbounded)

            // ----- Assert -----
            stats.totalPages shouldBe 300
        }

        @Test
        fun `falls back to the book pages when the edition has none or is not the readers`() {
            // ----- Arrange -----
            val noEditionPages = readBook(
                id = 1,
                finishedAt = "2026-02-01",
                pages = 200,
                editions = listOf(
                    edition(
                        id = 10,
                        pages = null,
                    ),
                ),
                editionId = 10,
            )
            val otherEdition = readBook(
                id = 2,
                finishedAt = "2026-02-01",
                pages = 100,
                editions = listOf(
                    edition(
                        id = 11,
                        pages = 999,
                    ),
                ),
                editionId = 12,
            )

            // ----- Act -----
            val stats = listOf(noEditionPages, otherEdition).toStats(StatsRange.Unbounded)

            // ----- Assert -----
            stats.totalPages shouldBe 300
        }

        @Test
        fun `counts zero pages when neither edition nor book has a count`() {
            // ----- Arrange -----
            val book = readBook(
                id = 1,
                finishedAt = "2026-02-01",
            )

            // ----- Act -----
            val stats = listOf(book).toStats(StatsRange.Unbounded)

            // ----- Assert -----
            stats.totalPages shouldBe 0
        }
    }

    @Nested
    inner class Genres {
        @Test
        fun `fractions are shares of the genre-tagged books and untagged books stay out`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-01-10",
                    tags = listOf(
                        genre("Fantasy"),
                        genre("Fantasy"),
                    ),
                ),
                readBook(
                    id = 2,
                    finishedAt = "2026-01-11",
                    tags = listOf(
                        genre("Fantasy"),
                        genre("Mystery"),
                    ),
                ),
                readBook(
                    id = 3,
                    finishedAt = "2026-01-12",
                ),
                readBook(
                    id = 4,
                    finishedAt = "2026-01-13",
                    tags = listOf(
                        Tag(
                            id = 1,
                            name = "Cozy",
                            category = TagCategory.OTHER,
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val genres = books.toStats(StatsRange.Unbounded).genres

            // ----- Assert -----
            genres.slices shouldContainExactly listOf(
                GenreSlice(
                    name = "Fantasy",
                    count = 2,
                    fraction = 1.0,
                ),
                GenreSlice(
                    name = "Mystery",
                    count = 1,
                    fraction = 0.5,
                ),
            )
        }

        @Test
        fun `case-differing spellings of one genre show the most frequent one whatever the book order`() {
            // ----- Arrange -----
            val lower1 = readBook(
                id = 1,
                finishedAt = "2026-01-10",
                tags = listOf(genre("Science fiction")),
            )
            val lower2 = readBook(
                id = 2,
                finishedAt = "2026-01-11",
                tags = listOf(genre("Science fiction")),
            )
            val upper = readBook(
                id = 3,
                finishedAt = "2026-01-12",
                tags = listOf(genre("Science Fiction")),
            )

            // ----- Act -----
            val upperLast = listOf(lower1, lower2, upper).toStats(StatsRange.Unbounded).genres
            val upperFirst = listOf(upper, lower1, lower2).toStats(StatsRange.Unbounded).genres

            // ----- Assert -----
            val expected = listOf(
                GenreSlice(
                    name = "Science fiction",
                    count = 3,
                    fraction = 1.0,
                ),
            )
            upperLast.slices shouldContainExactly expected
            upperFirst.slices shouldContainExactly expected
        }

        @Test
        fun `a one-to-one tie between case-differing spellings falls to the name order`() {
            // ----- Arrange -----
            val lower = readBook(
                id = 1,
                finishedAt = "2026-01-10",
                tags = listOf(genre("Science fiction")),
            )
            val upper = readBook(
                id = 2,
                finishedAt = "2026-01-11",
                tags = listOf(genre("Science Fiction")),
            )

            // ----- Act -----
            val lowerFirst = listOf(lower, upper).toStats(StatsRange.Unbounded).genres
            val upperFirst = listOf(upper, lower).toStats(StatsRange.Unbounded).genres

            // ----- Assert -----
            val expected = listOf(
                GenreSlice(
                    name = "Science Fiction",
                    count = 2,
                    fraction = 1.0,
                ),
            )
            lowerFirst.slices shouldContainExactly expected
            upperFirst.slices shouldContainExactly expected
        }

        @Test
        fun `a book counts only its ten most common genres and ties at the cut fall to the name`() {
            // ----- Arrange -----
            val popular = (1..9).map {
                genre(
                    "Popular$it",
                    count = 10,
                )
            }
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-01-10",
                    tags = popular + listOf(
                        genre(
                            "Cherry",
                            count = 1,
                        ),
                        genre(
                            "Apple",
                            count = 1,
                        ),
                        genre(
                            "Banana",
                            count = 1,
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val genres = books.toStats(StatsRange.Unbounded).genres

            // ----- Assert -----
            genres.slices.size shouldBe 10
            genres.slices.map { it.name } shouldContain "Apple"
            genres.slices.map { it.name } shouldNotContain "Banana"
            genres.slices.map { it.name } shouldNotContain "Cherry"
        }

        @Test
        fun `a user genre tag counts for its book`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-01-10",
                    tags = listOf(genre("Fantasy")),
                ),
            )
            val userTags = mapOf(
                1 to listOf(userTag("Mystery")),
            )

            // ----- Act -----
            val genres = books.toStats(
                StatsRange.Unbounded,
                userTags,
            ).genres

            // ----- Assert -----
            genres.slices.map { it.name } shouldContainExactly listOf("Fantasy", "Mystery")
        }

        @Test
        fun `a user tag outside the genre category is ignored`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-01-10",
                    tags = listOf(genre("Fantasy")),
                ),
            )
            val userTags = mapOf(
                1 to listOf(
                    userTag(
                        "Cozy",
                        TagCategory.MOOD,
                    ),
                ),
            )

            // ----- Act -----
            val genres = books.toStats(
                StatsRange.Unbounded,
                userTags,
            ).genres

            // ----- Assert -----
            genres.slices.map { it.name } shouldContainExactly listOf("Fantasy")
        }

        @Test
        fun `the same genre in the user and Hardcover spelling on one book counts once`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-01-10",
                    tags = listOf(genre("Fantasy")),
                ),
            )
            val userTags = mapOf(
                1 to listOf(userTag("fantasy")),
            )

            // ----- Act -----
            val genres = books.toStats(
                StatsRange.Unbounded,
                userTags,
            ).genres

            // ----- Assert -----
            genres.slices shouldContainExactly listOf(
                GenreSlice(
                    name = "Fantasy",
                    count = 1,
                    fraction = 1.0,
                ),
            )
        }

        @Test
        fun `the same genre across two books forms one slice shown in the Hardcover spelling`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-01-10",
                ),
                readBook(
                    id = 2,
                    finishedAt = "2026-01-11",
                    tags = listOf(genre("Fantasy")),
                ),
            )
            val userTags = mapOf(
                1 to listOf(userTag("fantasy")),
            )

            // ----- Act -----
            val genres = books.toStats(
                StatsRange.Unbounded,
                userTags,
            ).genres

            // ----- Assert -----
            genres.slices shouldContainExactly listOf(
                GenreSlice(
                    name = "Fantasy",
                    count = 2,
                    fraction = 1.0,
                ),
            )
        }

        @Test
        fun `a book with only user genre tags joins the denominator`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-01-10",
                    tags = listOf(genre("Fantasy")),
                ),
                readBook(
                    id = 2,
                    finishedAt = "2026-01-11",
                ),
            )
            val userTags = mapOf(
                2 to listOf(userTag("Horror")),
            )

            // ----- Act -----
            val genres = books.toStats(
                StatsRange.Unbounded,
                userTags,
            ).genres

            // ----- Assert -----
            genres.slices shouldContainExactly listOf(
                GenreSlice(
                    name = "Fantasy",
                    count = 1,
                    fraction = 0.5,
                ),
                GenreSlice(
                    name = "Horror",
                    count = 1,
                    fraction = 0.5,
                ),
            )
        }

        @Test
        fun `every genre is returned ordered by count then name`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-01-10",
                    tags = listOf("D", "C", "B", "A", "F", "E").map { genre(it) },
                ),
                readBook(
                    id = 2,
                    finishedAt = "2026-01-11",
                    tags = listOf(genre("E")),
                ),
            )

            // ----- Act -----
            val genres = books.toStats(StatsRange.Unbounded).genres

            // ----- Assert -----
            genres.slices.map { it.name } shouldContainExactly listOf("E", "A", "B", "C", "D", "F")
        }
    }

    @Nested
    inner class Ratings {
        @Test
        fun `histogram and average cover rated books only`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-01-10",
                    rating = 5.0,
                ),
                readBook(
                    id = 2,
                    finishedAt = "2026-01-11",
                    rating = 4.0,
                ),
                readBook(
                    id = 3,
                    finishedAt = "2026-01-12",
                    rating = 4.0,
                ),
                readBook(
                    id = 4,
                    finishedAt = "2026-01-13",
                ),
            )

            // ----- Act -----
            val ratings = books.toStats(StatsRange.Unbounded).ratings

            // ----- Assert -----
            ratings.totalRatings shouldBe 3
            ratings.average shouldBe 13.0 / 3
            ratings.halfStarBuckets shouldBe listOf(0, 0, 0, 0, 0, 0, 2, 0, 1)
        }

        @Test
        fun `a half-star rating folds into the lowest bucket`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-01-10",
                    rating = 0.5,
                ),
                readBook(
                    id = 2,
                    finishedAt = "2026-01-11",
                    rating = 1.0,
                ),
            )

            // ----- Act -----
            val ratings = books.toStats(StatsRange.Unbounded).ratings

            // ----- Assert -----
            ratings.halfStarBuckets shouldBe listOf(2, 0, 0, 0, 0, 0, 0, 0, 0)
        }
    }

    @Nested
    inner class AuthorDemographicsAggregation {
        @Test
        fun `an author on several books counts once`() {
            // ----- Arrange -----
            val author = Author(
                id = 1,
                name = "Sam",
                gender = Gender.Female,
            )
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-01-10",
                    authors = listOf(author),
                ),
                readBook(
                    id = 2,
                    finishedAt = "2026-01-11",
                    authors = listOf(author),
                ),
            )

            // ----- Act -----
            val demographics = books.toStats(StatsRange.Unbounded).authorDemographics

            // ----- Assert -----
            demographics.knownGenderCount shouldBe 1
            demographics.genderSlices.map { it.count } shouldBe listOf(1)
        }

        @Test
        fun `two authors sharing a name but not an id stay apart`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-01-10",
                    authors = listOf(
                        Author(
                            id = 2,
                            name = "Sam",
                            gender = Gender.Male,
                        ),
                        Author(
                            id = 3,
                            name = "Sam",
                            gender = Gender.Male,
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val demographics = books.toStats(StatsRange.Unbounded).authorDemographics

            // ----- Assert -----
            demographics.knownGenderCount shouldBe 2
            demographics.genderSlices.single().count shouldBe 2
            demographics.genderSlices.single().fraction shouldBe 1.0
        }

        @Test
        fun `unknown gender is its own bucket pinned last and apart from other`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-01-10",
                    authors = listOf(
                        Author(
                            id = 1,
                            name = "A",
                            gender = Gender.Unknown,
                        ),
                        Author(
                            id = 2,
                            name = "B",
                            gender = Gender.Other,
                        ),
                        Author(
                            id = 3,
                            name = "C",
                            gender = Gender.Male,
                        ),
                        Author(
                            id = 4,
                            name = "D",
                            gender = Gender.Male,
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val demographics = books.toStats(StatsRange.Unbounded).authorDemographics

            // ----- Assert -----
            demographics.genderSlices.map { it.gender } shouldBe listOf(Gender.Male, Gender.Other, Gender.Unknown)
            demographics.genderSlices.map { it.count } shouldBe listOf(2, 1, 1)
            demographics.knownGenderCount shouldBe 3
            demographics.unknownGenderCount shouldBe 1
        }

        @Test
        fun `bipoc and lgbtq breakdowns split yes, no and unknown across distinct authors`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-01-10",
                    authors = listOf(
                        Author(
                            id = 1,
                            name = "A",
                            isBipoc = true,
                            isLgbtq = false,
                        ),
                        Author(
                            id = 2,
                            name = "B",
                            isBipoc = false,
                            isLgbtq = null,
                        ),
                        Author(
                            id = 3,
                            name = "C",
                            isBipoc = null,
                            isLgbtq = null,
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val demographics = books.toStats(StatsRange.Unbounded).authorDemographics

            // ----- Assert -----
            demographics.bipocBreakdown shouldBe DemographicBreakdown(
                yesCount = 1,
                noCount = 1,
                unknownCount = 1,
            )
            demographics.lgbtqBreakdown shouldBe DemographicBreakdown(
                yesCount = 0,
                noCount = 1,
                unknownCount = 2,
            )
        }

        @Test
        fun `authors of books outside the range are ignored`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2020-01-10",
                    authors = listOf(
                        Author(
                            id = 1,
                            name = "Old",
                            gender = Gender.Male,
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val demographics = books.toStats(bounded).authorDemographics

            // ----- Assert -----
            demographics.genderSlices shouldBe emptyList()
            demographics.knownGenderCount shouldBe 0
        }
    }
}

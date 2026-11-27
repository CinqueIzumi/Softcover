package nl.rhaydus.softcover.core.profile.domain.mapper

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import nl.rhaydus.softcover.core.book.domain.model.ProgressUpdate
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.BookTraits
import nl.rhaydus.softcover.core.domain.model.CharacterTrait
import nl.rhaydus.softcover.core.domain.model.Gender
import nl.rhaydus.softcover.core.domain.model.Pace
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.domain.model.TraitAnswer
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.profile.domain.model.AuthorshipGender
import nl.rhaydus.softcover.core.profile.domain.model.CharacterBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.DemographicBreakdown
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DiversityStatsMapperTest {
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

    private fun List<Book>.toStats(
        range: StatsRange,
        updates: List<ProgressUpdate> = emptyList(),
        traitsByBookId: Map<Int, BookTraits> = emptyMap(),
    ) = toDiversityStats(
        range = range,
        updates = updates,
        traitsByBookId = traitsByBookId,
        timeZone = TimeZone.UTC,
    )

    private fun Book.withStatus(status: BookStatus): Book = copy(userBook = userBook?.copy(status = status))

    private fun reading(
        id: Int,
        startedAt: String? = null,
        reads: List<UserBookRead> = listOf(
            datedRead(
                id = id,
                startedAt = startedAt,
                finishedAt = null,
            ),
        ),
    ): Book = readBook(
        id = id,
        reads = reads,
    ).withStatus(BookStatus.Reading)

    @Nested
    inner class WindowRules {
        @Test
        fun `a read finished in the window counts`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-03-01",
                ),
            )

            // ----- Act -----
            val stats = books.toStats(bounded)

            // ----- Assert -----
            stats.booksRead shouldBe 1
        }

        @Test
        fun `a legacy finish date in the window counts a book without reads`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    lastReadDate = "2026-04-01",
                ),
            )

            // ----- Act -----
            val stats = books.toStats(bounded)

            // ----- Assert -----
            stats.booksRead shouldBe 1
        }

        @Test
        fun `a book still being read that was started in the window counts`() {
            // ----- Arrange -----
            val books = listOf(
                reading(
                    id = 1,
                    startedAt = "2026-05-01",
                ),
            )

            // ----- Act -----
            val stats = books.toStats(bounded)

            // ----- Assert -----
            stats.booksRead shouldBe 1
        }

        @Test
        fun `a did not finish book stopped in the window counts`() {
            // ----- Arrange -----
            val books = listOf(dnfBook(id = 1))

            // ----- Act -----
            val stats = books.toStats(bounded)

            // ----- Assert -----
            stats.booksRead shouldBe 1
        }

        @Test
        fun `a book still being read with a stop date in the window is out`() {
            // ----- Arrange -----
            val books = listOf(dnfBook(id = 1).withStatus(BookStatus.Reading))

            // ----- Act -----
            val stats = books.toStats(bounded)

            // ----- Assert -----
            stats.booksRead shouldBe 0
        }

        @Test
        fun `a finished book with a stop date in the window is out`() {
            // ----- Arrange -----
            val books = listOf(dnfBook(id = 1).withStatus(BookStatus.Read))

            // ----- Act -----
            val stats = books.toStats(bounded)

            // ----- Assert -----
            stats.booksRead shouldBe 0
        }

        @Test
        fun `a did not finish book stopped before the window is out`() {
            // ----- Arrange -----
            val books = listOf(
                dnfBook(
                    id = 1,
                    stoppedAt = "2024-03-12T12:00:00.000000",
                ),
            )

            // ----- Act -----
            val stats = books.toStats(bounded)

            // ----- Assert -----
            stats.booksRead shouldBe 0
        }

        @Test
        fun `progress logged in the window counts a book with no dates in it`() {
            // ----- Arrange -----
            val books = listOf(
                reading(
                    id = 1,
                    startedAt = "2024-05-01",
                ),
            )

            val updates = listOf(
                progressUpdate(
                    bookId = 1,
                    at = "2026-06-01T10:00:00Z",
                    pagesRead = 10,
                ),
            )

            // ----- Act -----
            val stats = books.toStats(
                range = bounded,
                updates = updates,
            )

            // ----- Assert -----
            stats.booksRead shouldBe 1
        }

        @Test
        fun `progress logged for another book does not count`() {
            // ----- Arrange -----
            val books = listOf(
                reading(
                    id = 1,
                    startedAt = "2024-05-01",
                ),
            )

            val updates = listOf(
                progressUpdate(
                    bookId = 2,
                    at = "2026-06-01T10:00:00Z",
                    pagesRead = 10,
                ),
            )

            // ----- Act -----
            val stats = books.toStats(
                range = bounded,
                updates = updates,
            )

            // ----- Assert -----
            stats.booksRead shouldBe 0
        }

        @Test
        fun `progress is placed on its local day at the window edges`() {
            // ----- Arrange -----
            val books = listOf(
                reading(
                    id = 1,
                    startedAt = "2024-05-01",
                ),
                reading(
                    id = 2,
                    startedAt = "2024-05-01",
                ),
            )

            val updates = listOf(
                progressUpdate(
                    bookId = 1,
                    at = "2025-12-31T23:59:59Z",
                    pagesRead = 10,
                ),
                progressUpdate(
                    bookId = 2,
                    at = "2026-01-01T00:00:00Z",
                    pagesRead = 10,
                ),
            )

            // ----- Act -----
            val stats = books.toStats(
                range = bounded,
                updates = updates,
            )

            // ----- Assert -----
            stats.booksRead shouldBe 1
        }

        @Test
        fun `a book read only before the window is out`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2025-12-31",
                ),
            )

            // ----- Act -----
            val stats = books.toStats(bounded)

            // ----- Assert -----
            stats.booksRead shouldBe 0
        }

        @Test
        fun `a re-read with an old finished read and progress in the window counts once`() {
            // ----- Arrange -----
            val books = listOf(
                reading(
                    id = 1,
                    reads = listOf(
                        unfinishedRead(id = 1),
                        finishedRead(
                            id = 2,
                            finishedAt = "2020-01-01",
                        ),
                    ),
                ),
            )

            val updates = listOf(
                progressUpdate(
                    bookId = 1,
                    at = "2026-02-01T10:00:00Z",
                    pagesRead = 10,
                ),
                progressUpdate(
                    bookId = 1,
                    at = "2026-02-02T10:00:00Z",
                    pagesRead = 10,
                ),
            )

            // ----- Act -----
            val stats = books.toStats(
                range = bounded,
                updates = updates,
            )

            // ----- Assert -----
            stats.booksRead shouldBe 1
        }

        @Test
        fun `a book matching several conditions counts once`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    reads = listOf(
                        datedRead(
                            id = 1,
                            startedAt = "2026-02-01",
                            finishedAt = "2026-03-01",
                        ),
                    ),
                    lastReadDate = "2026-03-01",
                ),
            )

            val updates = listOf(
                progressUpdate(
                    bookId = 1,
                    at = "2026-02-10T10:00:00Z",
                    pagesRead = 10,
                ),
            )

            // ----- Act -----
            val stats = books.toStats(
                range = bounded,
                updates = updates,
            )

            // ----- Assert -----
            stats.booksRead shouldBe 1
            stats.authorDemographics.unknownGenderCount shouldBe 1
        }

        @Test
        fun `a book without a user book is out of a bounded window`() {
            // ----- Arrange -----
            val books = listOf(readBook(id = 1).copy(userBook = null))

            // ----- Act -----
            val stats = books.toStats(bounded)

            // ----- Assert -----
            stats.booksRead shouldBe 0
        }

        @Test
        fun `unbounded counts every book whatever its status or dates`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2010-01-01",
                ),
                readBook(id = 2).withStatus(BookStatus.Paused),
                reading(id = 3),
                dnfBook(
                    id = 4,
                    stoppedAt = null,
                ),
            )

            // ----- Act -----
            val stats = books.toStats(StatsRange.Unbounded)

            // ----- Assert -----
            stats.booksRead shouldBe 4
        }
    }

    private fun author(
        id: Int,
        gender: Gender = Gender.Unknown,
        isBipoc: Boolean? = null,
        isLgbtq: Boolean? = null,
    ) = Author(
        id = id,
        name = "Author $id",
        isBipoc = isBipoc,
        isLgbtq = isLgbtq,
        gender = gender,
    )

    private fun demographicsOf(vararg authors: Author) = listOf(
        readBook(
            id = 1,
            finishedAt = "2026-01-10",
            authors = authors.toList(),
        ),
    ).toStats(StatsRange.Unbounded).authorDemographics

    @Nested
    inner class AuthorGenderPerBook {
        @Test
        fun `every primary author of one known gender gives the book that gender`() {
            // ----- Arrange -----
            val authors = listOf(
                author(
                    id = 1,
                    gender = Gender.Female,
                ),
                author(
                    id = 2,
                    gender = Gender.Female,
                ),
            )

            // ----- Act -----
            val demographics = demographicsOf(*authors.toTypedArray())

            // ----- Assert -----
            demographics.genderSlices.map { it.gender } shouldBe listOf(AuthorshipGender.Female)
            demographics.genderSlices.single().count shouldBe 1
            demographics.knownGenderCount shouldBe 1
        }

        @Test
        fun `known genders that differ make the book Mixed`() {
            // ----- Arrange -----
            val authors = listOf(
                author(
                    id = 1,
                    gender = Gender.Female,
                ),
                author(
                    id = 2,
                    gender = Gender.Male,
                ),
            )

            // ----- Act -----
            val demographics = demographicsOf(*authors.toTypedArray())

            // ----- Assert -----
            demographics.genderSlices.map { it.gender } shouldBe listOf(AuthorshipGender.Mixed)
        }

        @Test
        fun `differing known genders stay Mixed when another author is untagged`() {
            // ----- Arrange -----
            val authors = listOf(
                author(
                    id = 1,
                    gender = Gender.Female,
                ),
                author(
                    id = 2,
                    gender = Gender.Male,
                ),
                author(id = 3),
            )

            // ----- Act -----
            val demographics = demographicsOf(*authors.toTypedArray())

            // ----- Assert -----
            demographics.genderSlices.map { it.gender } shouldBe listOf(AuthorshipGender.Mixed)
        }

        @Test
        fun `one woman and one untagged author leave the book Unknown`() {
            // ----- Arrange -----
            val authors = listOf(
                author(
                    id = 1,
                    gender = Gender.Female,
                ),
                author(id = 2),
            )

            // ----- Act -----
            val demographics = demographicsOf(*authors.toTypedArray())

            // ----- Assert -----
            demographics.genderSlices.map { it.gender } shouldBe listOf(AuthorshipGender.Unknown)
            demographics.knownGenderCount shouldBe 0
            demographics.unknownGenderCount shouldBe 1
        }

        @Test
        fun `two women and one untagged author leave the book Unknown`() {
            // ----- Arrange -----
            val authors = listOf(
                author(
                    id = 1,
                    gender = Gender.Female,
                ),
                author(
                    id = 2,
                    gender = Gender.Female,
                ),
                author(id = 3),
            )

            // ----- Act -----
            val demographics = demographicsOf(*authors.toTypedArray())

            // ----- Assert -----
            demographics.genderSlices.map { it.gender } shouldBe listOf(AuthorshipGender.Unknown)
        }

        @Test
        fun `a book without primary authors is Unknown`() {
            // ----- Arrange -----
            val authors = emptyList<Author>()

            // ----- Act -----
            val demographics = demographicsOf(*authors.toTypedArray())

            // ----- Assert -----
            demographics.genderSlices.map { it.gender } shouldBe listOf(AuthorshipGender.Unknown)
            demographics.unknownGenderCount shouldBe 1
        }

        @Test
        fun `a non-primary contributor does not change the book's gender`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-01-10",
                    authors = listOf(
                        author(
                            id = 1,
                            gender = Gender.Female,
                        ),
                        author(
                            id = 2,
                            gender = Gender.Male,
                        ),
                    ),
                ).copy(nonPrimaryAuthorIds = setOf(2)),
            )

            // ----- Act -----
            val demographics = books.toStats(StatsRange.Unbounded).authorDemographics

            // ----- Assert -----
            demographics.genderSlices.map { it.gender } shouldBe listOf(AuthorshipGender.Female)
        }

        @Test
        fun `an author on several books counts once per book`() {
            // ----- Arrange -----
            val sam = author(
                id = 1,
                gender = Gender.Female,
            )

            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-01-10",
                    authors = listOf(sam),
                ),
                readBook(
                    id = 2,
                    finishedAt = "2026-01-11",
                    authors = listOf(sam),
                ),
            )

            // ----- Act -----
            val demographics = books.toStats(StatsRange.Unbounded).authorDemographics

            // ----- Assert -----
            demographics.knownGenderCount shouldBe 2
            demographics.genderSlices.single().count shouldBe 2
        }

        @Test
        fun `slices run known genders by count, then Mixed, then Unknown, over all books`() {
            // ----- Arrange -----
            val woman = author(
                id = 1,
                gender = Gender.Female,
            )

            val man = author(
                id = 2,
                gender = Gender.Male,
            )

            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-01-01",
                    authors = listOf(woman, man),
                ),
                readBook(
                    id = 2,
                    finishedAt = "2026-01-02",
                    authors = listOf(man),
                ),
                readBook(
                    id = 3,
                    finishedAt = "2026-01-03",
                    authors = listOf(woman),
                ),
                readBook(
                    id = 4,
                    finishedAt = "2026-01-04",
                    authors = listOf(man),
                ),
                readBook(
                    id = 5,
                    finishedAt = "2026-01-05",
                ),
            )

            // ----- Act -----
            val demographics = books.toStats(StatsRange.Unbounded).authorDemographics

            // ----- Assert -----
            demographics.genderSlices.map { it.gender } shouldBe listOf(
                AuthorshipGender.Male,
                AuthorshipGender.Female,
                AuthorshipGender.Mixed,
                AuthorshipGender.Unknown,
            )

            demographics.genderSlices.map { it.count } shouldBe listOf(2, 1, 1, 1)
            demographics.genderSlices.map { it.fraction } shouldBe listOf(0.4, 0.2, 0.2, 0.2)
            demographics.knownGenderCount shouldBe 4
            demographics.unknownGenderCount shouldBe 1
        }

        @Test
        fun `books outside the range are ignored`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2020-01-10",
                    authors = listOf(
                        author(
                            id = 1,
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

    @Nested
    inner class AuthorBipocAndLgbtqPerBook {
        @Test
        fun `any primary author marked yes makes the book yes`() {
            // ----- Arrange -----
            val authors = listOf(
                author(
                    id = 1,
                    isBipoc = true,
                    isLgbtq = false,
                ),
                author(
                    id = 2,
                    isBipoc = false,
                    isLgbtq = null,
                ),
                author(id = 3),
            )

            // ----- Act -----
            val demographics = demographicsOf(*authors.toTypedArray())

            // ----- Assert -----
            demographics.bipocBreakdown shouldBe DemographicBreakdown(yesCount = 1)
            demographics.lgbtqBreakdown shouldBe DemographicBreakdown(unknownCount = 1)
        }

        @Test
        fun `every primary author known not to be makes the book no`() {
            // ----- Arrange -----
            val authors = listOf(
                author(
                    id = 1,
                    isBipoc = false,
                    isLgbtq = false,
                ),
                author(
                    id = 2,
                    isBipoc = false,
                    isLgbtq = false,
                ),
            )

            // ----- Act -----
            val demographics = demographicsOf(*authors.toTypedArray())

            // ----- Assert -----
            demographics.bipocBreakdown shouldBe DemographicBreakdown(noCount = 1)
            demographics.lgbtqBreakdown shouldBe DemographicBreakdown(noCount = 1)
        }

        @Test
        fun `a mix of no and untagged authors leaves the book unknown`() {
            // ----- Arrange -----
            val authors = listOf(
                author(
                    id = 1,
                    isBipoc = false,
                ),
                author(id = 2),
            )

            // ----- Act -----
            val demographics = demographicsOf(*authors.toTypedArray())

            // ----- Assert -----
            demographics.bipocBreakdown shouldBe DemographicBreakdown(unknownCount = 1)
        }

        @Test
        fun `a book without primary authors is unknown and every book is counted once`() {
            // ----- Arrange -----
            val books = listOf(
                readBook(
                    id = 1,
                    finishedAt = "2026-01-01",
                ),
                readBook(
                    id = 2,
                    finishedAt = "2026-01-02",
                    authors = listOf(
                        author(
                            id = 1,
                            isBipoc = true,
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val demographics = books.toStats(StatsRange.Unbounded).authorDemographics

            // ----- Assert -----
            demographics.bipocBreakdown shouldBe DemographicBreakdown(
                yesCount = 1,
                unknownCount = 1,
            )
        }
    }

    @Nested
    inner class CharacterRepresentationCounts {
        private fun books(vararg ids: Int) = ids.map { id ->
            readBook(
                id = id,
                finishedAt = "2026-01-10",
            )
        }

        private fun traits(
            bookId: Int,
            vararg characters: Pair<CharacterTrait, TraitAnswer>,
            pace: Pace? = null,
        ) = BookTraits(
            bookId = bookId,
            pace = pace,
            characters = characters.toMap(),
        )

        @Test
        fun `a book without a traits row is not answered`() {
            // ----- Arrange -----
            val books = books(1)

            // ----- Act -----
            val representation = books.toStats(StatsRange.Unbounded).characterRepresentation

            // ----- Assert -----
            representation.bipoc shouldBe CharacterBreakdown(notAnsweredCount = 1)
            representation.lgbtq shouldBe CharacterBreakdown(notAnsweredCount = 1)
        }

        @Test
        fun `a row with only pace answered is not answered`() {
            // ----- Arrange -----
            val traitsByBookId = mapOf(
                1 to traits(
                    bookId = 1,
                    pace = Pace.FAST,
                ),
            )

            // ----- Act -----
            val representation = books(1).toStats(
                range = StatsRange.Unbounded,
                traitsByBookId = traitsByBookId,
            ).characterRepresentation

            // ----- Assert -----
            representation.bipoc shouldBe CharacterBreakdown(notAnsweredCount = 1)
            representation.lgbtq shouldBe CharacterBreakdown(notAnsweredCount = 1)
        }

        @Test
        fun `only LGBTQ marked yes counts the book as no for BIPOC and yes for LGBTQ`() {
            // ----- Arrange -----
            val traitsByBookId = mapOf(
                1 to traits(
                    1,
                    CharacterTrait.LGBTQ to TraitAnswer.YES,
                ),
            )

            // ----- Act -----
            val representation = books(1).toStats(
                range = StatsRange.Unbounded,
                traitsByBookId = traitsByBookId,
            ).characterRepresentation

            // ----- Assert -----
            representation.bipoc shouldBe CharacterBreakdown(noCount = 1)
            representation.lgbtq shouldBe CharacterBreakdown(yesCount = 1)
        }

        @Test
        fun `a complicated answer counts as complicated`() {
            // ----- Arrange -----
            val traitsByBookId = mapOf(
                1 to traits(
                    1,
                    CharacterTrait.BIPOC to TraitAnswer.COMPLICATED,
                ),
            )

            // ----- Act -----
            val representation = books(1).toStats(
                range = StatsRange.Unbounded,
                traitsByBookId = traitsByBookId,
            ).characterRepresentation

            // ----- Assert -----
            representation.bipoc shouldBe CharacterBreakdown(complicatedCount = 1)
            representation.lgbtq shouldBe CharacterBreakdown(noCount = 1)
        }

        @Test
        fun `traits of books outside the range or not read are not counted`() {
            // ----- Arrange -----
            val inRange = readBook(
                id = 1,
                finishedAt = "2026-03-01",
            )

            val outOfRange = readBook(
                id = 2,
                finishedAt = "2020-03-01",
            )

            val traitsByBookId = mapOf(
                1 to traits(
                    1,
                    CharacterTrait.BIPOC to TraitAnswer.YES,
                ),
                2 to traits(
                    2,
                    CharacterTrait.BIPOC to TraitAnswer.YES,
                ),
                3 to traits(
                    3,
                    CharacterTrait.BIPOC to TraitAnswer.YES,
                ),
            )

            // ----- Act -----
            val representation = listOf(inRange, outOfRange).toStats(
                range = bounded,
                traitsByBookId = traitsByBookId,
            ).characterRepresentation

            // ----- Assert -----
            representation.bipoc shouldBe CharacterBreakdown(yesCount = 1)
            representation.bipoc.total shouldBe 1
        }
    }
}

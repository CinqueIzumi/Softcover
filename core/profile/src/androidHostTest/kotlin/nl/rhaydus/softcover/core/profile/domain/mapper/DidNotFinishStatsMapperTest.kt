package nl.rhaydus.softcover.core.profile.domain.mapper

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.DidNotFinishReason
import nl.rhaydus.softcover.core.domain.model.DidNotFinishReasons
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.domain.model.Tag
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenreSlice
import nl.rhaydus.softcover.core.profile.domain.model.StopQuarter
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DidNotFinishStatsMapperTest {
    private val march = StatsRange.Bounded(
        start = LocalDate(
            year = 2026,
            month = 3,
            day = 1,
        ),
        endInclusive = LocalDate(
            year = 2026,
            month = 3,
            day = 31,
        ),
    )

    private fun stopped(
        id: Int,
        stoppedAt: String? = "2026-03-15T12:00:00.000000",
        progress: Float = 10f,
        tags: List<Tag> = emptyList(),
    ): Book = dnfBook(
        id = id,
        stoppedAt = stoppedAt,
        pages = 300,
        tags = tags,
        read = unfinishedRead(
            id = id,
            progress = progress,
        ),
    )

    private fun genre(name: String) = Tag(
        id = name.hashCode(),
        name = name,
        category = TagCategory.GENRE,
        count = 10,
    )

    @Nested
    inner class ToDidNotFinishStats {
        @Test
        fun `a bounded range counts the books stopped inside it and drops undated ones`() {
            // ----- Arrange -----
            val books = listOf(
                stopped(
                    id = 1,
                    stoppedAt = "2026-03-01T12:00:00.000000",
                ),
                stopped(
                    id = 2,
                    stoppedAt = "2026-03-31T12:00:00.000000",
                ),
                stopped(
                    id = 3,
                    stoppedAt = "2026-04-01T12:00:00.000000",
                ),
                stopped(
                    id = 4,
                    stoppedAt = "2026-02-28T12:00:00.000000",
                ),
                stopped(
                    id = 5,
                    stoppedAt = null,
                ),
            )

            // ----- Act -----
            val result = books.toDidNotFinishStats(
                range = march,
                userTagsByBookId = emptyMap(),
                reasonsByBookId = emptyMap(),
            )

            // ----- Assert -----
            result.bookCount shouldBe 2
        }

        @Test
        fun `an unbounded range counts every book including undated ones`() {
            // ----- Arrange -----
            val books = listOf(
                stopped(
                    id = 1,
                    stoppedAt = "2020-03-01T12:00:00.000000",
                ),
                stopped(
                    id = 2,
                    stoppedAt = null,
                ),
            )

            // ----- Act -----
            val result = books.toDidNotFinishStats(
                range = StatsRange.Unbounded,
                userTagsByBookId = emptyMap(),
                reasonsByBookId = emptyMap(),
            )

            // ----- Assert -----
            result.bookCount shouldBe 2
        }

        @Test
        fun `counts books per stop quarter and lists every quarter`() {
            // ----- Arrange -----
            val books = listOf(
                stopped(
                    id = 1,
                    progress = 10f,
                ),
                stopped(
                    id = 2,
                    progress = 20f,
                ),
                stopped(
                    id = 3,
                    progress = 90f,
                ),
            )

            // ----- Act -----
            val result = books.toDidNotFinishStats(
                range = march,
                userTagsByBookId = emptyMap(),
                reasonsByBookId = emptyMap(),
            )

            // ----- Assert -----
            result.quarterCounts shouldBe mapOf(
                StopQuarter.FIRST to 2,
                StopQuarter.SECOND to 0,
                StopQuarter.THIRD to 0,
                StopQuarter.LAST to 1,
            )
        }

        @Test
        fun `a book whose stop cannot be placed counts as unknown, not in any quarter`() {
            // ----- Arrange -----
            val books = listOf(
                stopped(
                    id = 1,
                    progress = 60f,
                ),
                stopped(
                    id = 2,
                    progress = 0f,
                ),
            )

            // ----- Act -----
            val result = books.toDidNotFinishStats(
                range = march,
                userTagsByBookId = emptyMap(),
                reasonsByBookId = emptyMap(),
            )

            // ----- Assert -----
            result.unknownStopCount shouldBe 1
            result.quarterCounts.values.sum() shouldBe 1
            result.quarterCounts.values.sum() + result.unknownStopCount shouldBe result.bookCount
        }

        @Test
        fun `places books in all four quarters and counts unplaceable ones separately`() {
            // ----- Arrange -----
            val books = listOf(
                stopped(
                    id = 1,
                    progress = 10f,
                ),
                stopped(
                    id = 2,
                    progress = 30f,
                ),
                stopped(
                    id = 3,
                    progress = 60f,
                ),
                stopped(
                    id = 4,
                    progress = 90f,
                ),
                stopped(
                    id = 5,
                    progress = 0f,
                ),
                stopped(
                    id = 6,
                    progress = 0f,
                ),
            )

            // ----- Act -----
            val result = books.toDidNotFinishStats(
                range = march,
                userTagsByBookId = emptyMap(),
                reasonsByBookId = emptyMap(),
            )

            // ----- Assert -----
            result.quarterCounts shouldBe mapOf(
                StopQuarter.FIRST to 1,
                StopQuarter.SECOND to 1,
                StopQuarter.THIRD to 1,
                StopQuarter.LAST to 1,
            )

            result.unknownStopCount shouldBe 2
            result.bookCount shouldBe 6
        }

        @Test
        fun `ranks genres over the books in range only`() {
            // ----- Arrange -----
            val books = listOf(
                stopped(
                    id = 1,
                    tags = listOf(genre("Fantasy")),
                ),
                stopped(
                    id = 2,
                    stoppedAt = "2025-03-15T12:00:00.000000",
                    tags = listOf(genre("Horror")),
                ),
                stopped(
                    id = 3,
                    stoppedAt = null,
                    tags = listOf(genre("Romance")),
                ),
            )

            // ----- Act -----
            val result = books.toDidNotFinishStats(
                range = march,
                userTagsByBookId = emptyMap(),
                reasonsByBookId = emptyMap(),
            )

            // ----- Assert -----
            result.genres shouldBe GenreBreakdown(
                slices = listOf(
                    GenreSlice(
                        name = "Fantasy",
                        count = 1,
                        fraction = 1.0,
                    ),
                ),
            )
        }
    }

    @Nested
    inner class Reasons {
        @Test
        fun `a book counts once under each reason it gave`() {
            // ----- Arrange -----
            val books = listOf(stopped(id = 1), stopped(id = 2))

            val reasons = mapOf(
                1 to DidNotFinishReasons(
                    bookId = 1,
                    reasons = setOf(DidNotFinishReason.TooSlow, DidNotFinishReason.LostInterest),
                ),
                2 to DidNotFinishReasons(
                    bookId = 2,
                    reasons = setOf(DidNotFinishReason.TooSlow),
                ),
            )

            // ----- Act -----
            val result = books.toDidNotFinishStats(
                range = march,
                userTagsByBookId = emptyMap(),
                reasonsByBookId = reasons,
            )

            // ----- Assert -----
            result.reasonCounts[DidNotFinishReason.TooSlow] shouldBe 2
            result.reasonCounts[DidNotFinishReason.LostInterest] shouldBe 1
            result.reasonCounts[DidNotFinishReason.UnwantedContent] shouldBe 0
            result.otherReasonCount shouldBe 0
        }

        @Test
        fun `a book with only its own words counts as something else`() {
            // ----- Arrange -----
            val books = listOf(stopped(id = 1), stopped(id = 2))

            val reasons = mapOf(
                1 to DidNotFinishReasons(
                    bookId = 1,
                    ownWords = "The ending was spoiled",
                ),
                2 to DidNotFinishReasons(
                    bookId = 2,
                    reasons = setOf(DidNotFinishReason.TooSlow),
                    ownWords = "Too many names",
                ),
            )

            // ----- Act -----
            val result = books.toDidNotFinishStats(
                range = march,
                userTagsByBookId = emptyMap(),
                reasonsByBookId = reasons,
            )

            // ----- Assert -----
            result.otherReasonCount shouldBe 1
            result.reasonCounts[DidNotFinishReason.TooSlow] shouldBe 1
        }

        @Test
        fun `a book with no reason is left out of every count`() {
            // ----- Arrange -----
            val books = listOf(stopped(id = 1), stopped(id = 2))

            val reasons = mapOf(2 to DidNotFinishReasons(bookId = 2))

            // ----- Act -----
            val result = books.toDidNotFinishStats(
                range = march,
                userTagsByBookId = emptyMap(),
                reasonsByBookId = reasons,
            )

            // ----- Assert -----
            result.bookCount shouldBe 2
            result.reasonCounts.values.sum() shouldBe 0
            result.otherReasonCount shouldBe 0
        }

        @Test
        fun `reasons of a book stopped outside the range are ignored`() {
            // ----- Arrange -----
            val books = listOf(
                stopped(id = 1),
                stopped(
                    id = 2,
                    stoppedAt = "2026-04-01T12:00:00.000000",
                ),
                stopped(
                    id = 3,
                    stoppedAt = null,
                ),
            )

            val reasons = (1..3).associateWith {
                DidNotFinishReasons(
                    bookId = it,
                    reasons = setOf(DidNotFinishReason.NotTheRightTime),
                    ownWords = "x",
                )
            }

            // ----- Act -----
            val result = books.toDidNotFinishStats(
                range = march,
                userTagsByBookId = emptyMap(),
                reasonsByBookId = reasons,
            )

            // ----- Assert -----
            result.reasonCounts[DidNotFinishReason.NotTheRightTime] shouldBe 1
        }
    }
}

package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLinePart
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.domain.model.StatsWindow
import nl.rhaydus.softcover.core.profile.domain.model.LongestBook
import nl.rhaydus.softcover.core.profile.domain.model.QuickestRead
import nl.rhaydus.softcover.core.profile.domain.model.ReadDuration
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStats
import nl.rhaydus.softcover.core.profile.domain.model.TimeToFinish
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Locale

class ReadingRecordsMapperTest {
    private lateinit var originalLocale: Locale

    @BeforeEach
    fun setUp() {
        originalLocale = Locale.getDefault()
        Locale.setDefault(Locale.UK)
    }

    @AfterEach
    fun tearDown() {
        Locale.setDefault(originalLocale)
    }

    private fun book(id: Int): Book {
        val edition = mockk<BookEdition> {
            every {
                this@mockk.id
            } returns 99

            every {
                url
            } returns "https://example.com/$id.jpg"

            every {
                localImagePath
            } returns null
        }

        return mockk {
            every {
                this@mockk.id
            } returns id

            every {
                title
            } returns "Book $id"

            every {
                currentEdition
            } returns edition

            every {
                defaultEdition
            } returns null

            every {
                coverUrl
            } returns "https://example.com/$id.jpg"
        }
    }

    private fun stats(
        longest: Book? = null,
        pages: Int = 1296,
        booksRead: Int = 3,
    ) = ReadingStats(
        booksRead = booksRead,
        longestBook = longest?.let {
            LongestBook(
                book = it,
                pages = pages,
            )
        },
    )

    private fun timeToFinish(
        quickest: Book? = null,
        pages: Int? = 272,
        days: Int = 2,
    ) = TimeToFinish(
        readCount = 1,
        medianDays = 5,
        bandMedianDays = emptyMap(),
        excludedReadCount = 0,
        quickestRead = quickest?.let {
            QuickestRead(
                book = it,
                duration = ReadDuration(
                    bookId = it.id,
                    readId = 1,
                    startedOn = LocalDate(
                        2026,
                        3,
                        1,
                    ),
                    finishedOn = LocalDate(
                        2026,
                        3,
                        days,
                    ),
                    pages = pages,
                ),
            )
        },
    )

    private fun BookCardUiModel.line(): String {
        val tile = variant as BookCardVariant.Tile
        val part = tile.text.supportingLines.single().single() as BookCardLinePart.Text

        return part.text
    }

    private fun BookCardUiModel.tile(): BookCardVariant.Tile = variant as BookCardVariant.Tile

    @Nested
    inner class ReadingRecordsUiModelTests {
        @Test
        fun `null stats yields the loading stand-in`() {
            // ----- Arrange -----
            val timeToFinish = timeToFinish()

            // ----- Act -----
            val result = readingRecordsUiModel(
                stats = null,
                timeToFinish = timeToFinish,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result.isLoading shouldBe true
            result.hasRecords shouldBe false
            result.cards.cards.size shouldBe 2
        }

        @Test
        fun `null time to finish yields the loading stand-in`() {
            // ----- Arrange -----
            val stats = stats(longest = book(1))

            // ----- Act -----
            val result = readingRecordsUiModel(
                stats = stats,
                timeToFinish = null,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result.isLoading shouldBe true
            result.hasRecords shouldBe false
        }

        @Test
        fun `lists the longest card then the quickest card with their lines`() {
            // ----- Arrange -----
            val stats = stats(longest = book(1))
            val timeToFinish = timeToFinish(quickest = book(2))

            // ----- Act -----
            val result = readingRecordsUiModel(
                stats = stats,
                timeToFinish = timeToFinish,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result.isLoading shouldBe false
            result.hasRecords shouldBe true

            result.cards.cards.map { it.key } shouldBe
                listOf(BookCardKey.Book(bookId = 1), BookCardKey.Book(bookId = 2))

            result.cards.cards.map { it.line() } shouldBe listOf("1,296 pages", "272 pages in 2 days")
            result.caption shouldBe ""
        }

        @Test
        fun `only the quickest record yields a single card`() {
            // ----- Arrange -----
            val timeToFinish = timeToFinish(
                quickest = book(2),
                days = 1,
            )

            // ----- Act -----
            val result = readingRecordsUiModel(
                stats = stats(),
                timeToFinish = timeToFinish,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result.cards.cards.map { it.key } shouldBe listOf(BookCardKey.Book(bookId = 2))
            result.cards.cards.single().line() shouldBe "272 pages in 1 day"
        }

        @Test
        fun `a quickest read without pages says only how long it took`() {
            // ----- Arrange -----
            val timeToFinish = timeToFinish(
                quickest = book(2),
                pages = null,
                days = 1,
            )

            // ----- Act -----
            val result = readingRecordsUiModel(
                stats = stats(),
                timeToFinish = timeToFinish,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result.cards.cards.single().line() shouldBe "In 1 day"
        }

        @Test
        fun `a quickest read without pages pluralises the days`() {
            // ----- Arrange -----
            val timeToFinish = timeToFinish(
                quickest = book(2),
                pages = null,
                days = 2,
            )

            // ----- Act -----
            val result = readingRecordsUiModel(
                stats = stats(),
                timeToFinish = timeToFinish,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result.cards.cards.single().line() shouldBe "In 2 days"
        }

        @Test
        fun `the set resolves each card key to its book`() {
            // ----- Arrange -----
            val longest = book(1)
            val quickest = book(2)

            // ----- Act -----
            val result = readingRecordsUiModel(
                stats = stats(longest = longest),
                timeToFinish = timeToFinish(quickest = quickest),
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result.cards[BookCardKey.Book(bookId = 1)].shouldNotBeNull().book shouldBe longest
            result.cards[BookCardKey.Book(bookId = 2)].shouldNotBeNull().book shouldBe quickest
        }

        @Test
        fun `distinct books both carry the shared transition keys`() {
            // ----- Arrange -----
            val stats = stats(longest = book(1))
            val timeToFinish = timeToFinish(quickest = book(2))

            // ----- Act -----
            val result = readingRecordsUiModel(
                stats = stats,
                timeToFinish = timeToFinish,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result.cards.cards.forEach {
                it.tile().cover.model.sharedTransitionKey.shouldNotBeNull()
                it.tile().text.titleTransitionKey.shouldNotBeNull()
            }
        }

        @Test
        fun `the same book as longest and quickest shares its transition keys on the longest card only`() {
            // ----- Arrange -----
            val shared = book(1)

            // ----- Act -----
            val result = readingRecordsUiModel(
                stats = stats(longest = shared),
                timeToFinish = timeToFinish(quickest = shared),
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            val (longestCard, quickestCard) = result.cards.cards.map { it.tile() }

            longestCard.cover.model.sharedTransitionKey.shouldNotBeNull()
            longestCard.text.titleTransitionKey.shouldNotBeNull()
            quickestCard.cover.model.sharedTransitionKey.shouldBeNull()
            quickestCard.text.titleTransitionKey.shouldBeNull()
        }
    }

    @Nested
    inner class CaptionTests {
        @Test
        fun `only the longest record explains the missing quickest`() {
            // ----- Arrange -----
            val stats = stats(longest = book(1))

            // ----- Act -----
            val result = readingRecordsUiModel(
                stats = stats,
                timeToFinish = timeToFinish(),
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result.caption shouldBe "None of these reads has both a start and a finish date."
        }

        @Test
        fun `only the quickest record with books read explains the missing page count`() {
            // ----- Arrange -----
            val timeToFinish = timeToFinish(quickest = book(2))

            // ----- Act -----
            val result = readingRecordsUiModel(
                stats = stats(booksRead = 2),
                timeToFinish = timeToFinish,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result.caption shouldBe "None of these books has a known page count."
        }

        @Test
        fun `only the quickest record with no books read has no caption`() {
            // ----- Arrange -----
            val timeToFinish = timeToFinish(quickest = book(2))

            // ----- Act -----
            val result = readingRecordsUiModel(
                stats = stats(booksRead = 0),
                timeToFinish = timeToFinish,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result.caption shouldBe ""
        }

        @Test
        fun `no records with books read explains both gaps`() {
            // ----- Arrange -----
            val stats = stats(booksRead = 4)

            // ----- Act -----
            val result = readingRecordsUiModel(
                stats = stats,
                timeToFinish = timeToFinish(),
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result.hasRecords shouldBe false
            result.caption shouldBe "None of these books has a page count or both reading dates."
        }

        @Test
        fun `no records and no books in a calendar year names the year`() {
            // ----- Arrange -----
            val stats = stats(booksRead = 0)

            // ----- Act -----
            val result = readingRecordsUiModel(
                stats = stats,
                timeToFinish = timeToFinish(),
                scope = StatsScope.CalendarYear(2025),
            )

            // ----- Assert -----
            result.caption shouldBe "No finished reads in 2025."
        }

        @Test
        fun `no records and no books in a rolling window names the window`() {
            // ----- Arrange -----
            val stats = stats(booksRead = 0)

            // ----- Act -----
            val result = readingRecordsUiModel(
                stats = stats,
                timeToFinish = timeToFinish(),
                scope = StatsScope.Rolling(StatsWindow.LAST_30_DAYS),
            )

            // ----- Assert -----
            result.caption shouldBe "No finished reads in the last 30 days."
        }

        @Test
        fun `no records and no books across all time invites the first finish`() {
            // ----- Arrange -----
            val stats = stats(booksRead = 0)

            // ----- Act -----
            val result = readingRecordsUiModel(
                stats = stats,
                timeToFinish = timeToFinish(),
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result.caption shouldBe "Finish a few books and your records show up here."
        }
    }
}

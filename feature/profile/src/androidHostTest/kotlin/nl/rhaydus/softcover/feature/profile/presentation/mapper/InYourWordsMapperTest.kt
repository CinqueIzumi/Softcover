package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.component.quote.QuoteCardUiModel
import nl.rhaydus.softcover.core.component.statistic.StatTileUiModel
import nl.rhaydus.softcover.core.component.statistic.StatTileVariant
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.Quote
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.domain.model.StatsWindow
import nl.rhaydus.softcover.core.profile.domain.model.MostQuotedBook
import nl.rhaydus.softcover.core.profile.domain.model.WrittenStats
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class InYourWordsMapperTest {
    private fun written(
        reviewCount: Int = 0,
        reviewWordCount: Int = 0,
        quoteCount: Int = 0,
        quotedBookCount: Int = 0,
        mostQuoted: MostQuotedBook? = null,
    ): WrittenStats = WrittenStats(
        reviewCount = reviewCount,
        reviewWordCount = reviewWordCount,
        quoteCount = quoteCount,
        quotedBookCount = quotedBookCount,
        mostQuoted = mostQuoted,
    )

    private fun mostQuoted(
        quoteCount: Int,
        authorName: String? = "Ursula K. Le Guin",
    ): MostQuotedBook {
        val bookAuthors = listOfNotNull(
            authorName?.let {
                Author(
                    id = 1,
                    name = it,
                )
            },
        )

        val book = mockk<Book> {
            every {
                id
            } returns 42

            every {
                title
            } returns "The Dispossessed"

            every {
                primaryAuthors
            } returns bookAuthors

            every {
                currentEdition
            } returns null

            every {
                defaultEdition
            } returns null

            every {
                coverUrl
            } returns "https://example.com/cover.jpg"
        }

        val quote = mockk<Quote> {
            every {
                text
            } returns "A first line."
        }

        return MostQuotedBook(
            book = book,
            quoteCount = quoteCount,
            firstQuote = quote,
        )
    }

    private fun StatTileUiModel.caption(): String = (variant as StatTileVariant.Standard).caption

    @Nested
    inner class Loading {
        @Test
        fun `null written stats yield the masked chapter`() {
            // ----- Arrange -----
            val scope = StatsScope.AllTime

            // ----- Act -----
            val result = inYourWordsUiModel(
                written = null,
                scope = scope,
            )

            // ----- Assert -----
            result.isLoading shouldBe true
            result.tiles.all { it.stat?.isLoading == true } shouldBe true
            result.mostQuoted.shouldNotBeNull()
        }

        @Test
        fun `loaded written stats are not loading`() {
            // ----- Arrange -----
            val stats = written(reviewCount = 1)

            // ----- Act -----
            val result = inYourWordsUiModel(
                written = stats,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result.isLoading shouldBe false
            result.tiles.none { it.stat?.isLoading == true } shouldBe true
        }
    }

    @Nested
    inner class Tiles {
        @Test
        fun `review tile counts reviews and captions the words with a grouped number`() {
            // ----- Arrange -----
            val stats = written(
                reviewCount = 4,
                reviewWordCount = 3200,
            )

            // ----- Act -----
            val result = inYourWordsUiModel(
                written = stats,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            val tile = result.tiles[0]
            tile.stat.shouldNotBeNull().value shouldBe 4.0
            tile.caption() shouldBe "3,200 words in all"
        }

        @Test
        fun `review caption is singular for one word`() {
            // ----- Arrange -----
            val stats = written(
                reviewCount = 1,
                reviewWordCount = 1,
            )

            // ----- Act -----
            val result = inYourWordsUiModel(
                written = stats,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result.tiles[0].caption() shouldBe "1 word in all"
        }

        @Test
        fun `quote tile counts quotes and captions the books they come from`() {
            // ----- Arrange -----
            val stats = written(
                quoteCount = 9,
                quotedBookCount = 3,
            )

            // ----- Act -----
            val result = inYourWordsUiModel(
                written = stats,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            val tile = result.tiles[1]
            tile.stat.shouldNotBeNull().value shouldBe 9.0
            tile.caption() shouldBe "saved from 3 books"
        }

        @Test
        fun `quote caption is singular for one book`() {
            // ----- Arrange -----
            val stats = written(
                quoteCount = 2,
                quotedBookCount = 1,
            )

            // ----- Act -----
            val result = inYourWordsUiModel(
                written = stats,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result.tiles[1].caption() shouldBe "saved from 1 book"
        }

        @Test
        fun `zero counts in a narrowed scope name the scope`() {
            // ----- Arrange -----
            val scope = StatsScope.CalendarYear(2025)

            // ----- Act -----
            val result = inYourWordsUiModel(
                written = written(),
                scope = scope,
            )

            // ----- Assert -----
            result.tiles[0].caption() shouldBe "None written in 2025"
            result.tiles[1].caption() shouldBe "None saved in 2025"
        }

        @Test
        fun `zero counts in a rolling window name the window`() {
            // ----- Arrange -----
            val scope = StatsScope.Rolling(StatsWindow.LAST_30_DAYS)

            // ----- Act -----
            val result = inYourWordsUiModel(
                written = written(),
                scope = scope,
            )

            // ----- Assert -----
            result.tiles[0].caption() shouldBe "None written in the last 30 days"
            result.tiles[1].caption() shouldBe "None saved in the last 30 days"
        }

        @Test
        fun `zero counts over all time say yet`() {
            // ----- Arrange -----
            val scope = StatsScope.AllTime

            // ----- Act -----
            val result = inYourWordsUiModel(
                written = written(),
                scope = scope,
            )

            // ----- Assert -----
            result.tiles[0].caption() shouldBe "None written yet"
            result.tiles[1].caption() shouldBe "None saved yet"
        }
    }

    @Nested
    inner class HasContent {
        @Test
        fun `is false when there are no reviews and no quotes`() {
            // ----- Arrange -----
            val stats = written()

            // ----- Act -----
            val result = inYourWordsUiModel(
                written = stats,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result.hasContent shouldBe false
        }

        @Test
        fun `is true with only reviews`() {
            // ----- Arrange -----
            val stats = written(reviewCount = 1)

            // ----- Act -----
            val result = inYourWordsUiModel(
                written = stats,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result.hasContent shouldBe true
        }

        @Test
        fun `is true with only quotes`() {
            // ----- Arrange -----
            val stats = written(quoteCount = 1)

            // ----- Act -----
            val result = inYourWordsUiModel(
                written = stats,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result.hasContent shouldBe true
        }
    }

    @Nested
    inner class MostQuoted {
        @Test
        fun `is absent without a most-quoted book`() {
            // ----- Arrange -----
            val stats = written(
                quoteCount = 2,
                mostQuoted = null,
            )

            // ----- Act -----
            val result = inYourWordsUiModel(
                written = stats,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result.mostQuoted.shouldBeNull()
        }

        @Test
        fun `captions several lines and points at the book first`() {
            // ----- Arrange -----
            val stats = written(
                quoteCount = 5,
                mostQuoted = mostQuoted(quoteCount = 5),
            )

            // ----- Act -----
            val result = inYourWordsUiModel(
                written = stats,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            val model = result.mostQuoted.shouldNotBeNull()
            model.caption shouldBe "You saved 5 lines from The Dispossessed. This one first."
            model.bookId shouldBe 42
        }

        @Test
        fun `captions a single line without the second sentence`() {
            // ----- Arrange -----
            val stats = written(
                quoteCount = 1,
                mostQuoted = mostQuoted(quoteCount = 1),
            )

            // ----- Act -----
            val result = inYourWordsUiModel(
                written = stats,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result.mostQuoted.shouldNotBeNull().caption shouldBe "You saved 1 line from The Dispossessed."
        }

        @Test
        fun `card caption joins the author and the quote count`() {
            // ----- Arrange -----
            val stats = written(
                quoteCount = 3,
                mostQuoted = mostQuoted(quoteCount = 3),
            )

            // ----- Act -----
            val result = inYourWordsUiModel(
                written = stats,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            val card = result.mostQuoted.shouldNotBeNull().card
            card.text shouldBe "A first line."
            card.caption shouldBe "Ursula K. Le Guin · 3 quotes"
        }

        @Test
        fun `card caption is the count alone without an author`() {
            // ----- Arrange -----
            val stats = written(
                quoteCount = 1,
                mostQuoted = mostQuoted(
                    quoteCount = 1,
                    authorName = null,
                ),
            )

            // ----- Act -----
            val result = inYourWordsUiModel(
                written = stats,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            val card: QuoteCardUiModel.Book = result.mostQuoted.shouldNotBeNull().card
            card.caption shouldBe "1 quote"
        }
    }
}

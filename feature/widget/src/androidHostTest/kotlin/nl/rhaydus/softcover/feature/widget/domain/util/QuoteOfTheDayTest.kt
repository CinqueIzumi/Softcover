package nl.rhaydus.softcover.feature.widget.domain.util

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import nl.rhaydus.softcover.core.domain.model.Quote
import nl.rhaydus.softcover.feature.widget.testQuote
import nl.rhaydus.softcover.feature.widget.testQuotes
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class QuoteOfTheDayTest {
    private val today = 20_000L

    @Nested
    inner class QuoteKey {
        @Test
        fun `is the same for the same book time and text whatever the ids`() {
            // ----- Arrange -----
            val local = testQuote(
                bookId = 7,
                text = "Same words",
                actionAtMillis = 1_000L,
                localId = 1L,
                remoteId = null,
            )
            val synced = testQuote(
                bookId = 7,
                text = "Same words",
                actionAtMillis = 1_000L,
                localId = 99L,
                remoteId = 555L,
            )

            // ----- Act -----
            val localKey = quoteKey(local)
            val syncedKey = quoteKey(synced)

            // ----- Assert -----
            localKey shouldBe syncedKey
        }

        @Test
        fun `differs when the text the book or the time differs`() {
            // ----- Arrange -----
            val base = testQuote(
                bookId = 7,
                text = "Same words",
                actionAtMillis = 1_000L,
            )
            val otherText = base.copy(text = "Other words")
            val otherBook = base.copy(bookId = 8)
            val otherTime = testQuote(
                bookId = 7,
                text = "Same words",
                actionAtMillis = 2_000L,
            )

            // ----- Act -----
            val keys = listOf(base, otherText, otherBook, otherTime).map(::quoteKey)

            // ----- Assert -----
            keys.toSet().size shouldBe 4
        }
    }

    @Nested
    inner class QuoteId {
        @Test
        fun `is the same for the same book time and text whatever the ids`() {
            // ----- Arrange -----
            val local = testQuote(
                bookId = 7,
                text = "Same words",
                actionAtMillis = 1_000L,
                localId = 1L,
                remoteId = null,
            )
            val synced = testQuote(
                bookId = 7,
                text = "Same words",
                actionAtMillis = 1_000L,
                localId = 99L,
                remoteId = 555L,
            )

            // ----- Act -----
            val localId = quoteId(local)
            val syncedId = quoteId(synced)

            // ----- Assert -----
            localId shouldBe syncedId
        }

        @Test
        fun `differs when the text differs`() {
            // ----- Arrange -----
            val base = testQuote(text = "Same words")
            val other = base.copy(text = "Other words")

            // ----- Act -----
            val baseId = quoteId(base)
            val otherId = quoteId(other)

            // ----- Assert -----
            baseId shouldNotBe otherId
        }

        @Test
        fun `is a compact id rather than the quote text`() {
            // ----- Arrange -----
            val long = testQuote(text = "word ".repeat(500))

            // ----- Act -----
            val id = quoteId(long)

            // ----- Assert -----
            id.length shouldBeLessThan 30
        }
    }

    @Nested
    inner class RankForDay {
        @Test
        fun `ranks the same quotes the same way every time`() {
            // ----- Arrange -----
            val quotes = testQuotes(count = 20)

            // ----- Act -----
            val first = rankForDay(
                quotes = quotes,
                epochDay = today,
            )
            val second = rankForDay(
                quotes = quotes,
                epochDay = today,
            )

            // ----- Assert -----
            second.map(::quoteKey) shouldContainExactly first.map(::quoteKey)
        }

        @Test
        fun `ranks the same whatever order the quotes arrive in`() {
            // ----- Arrange -----
            val quotes = testQuotes(count = 20)

            // ----- Act -----
            val forward = rankForDay(
                quotes = quotes,
                epochDay = today,
            )
            val reversed = rankForDay(
                quotes = quotes.reversed(),
                epochDay = today,
            )
            val rotated = rankForDay(
                quotes = quotes.drop(7) + quotes.take(7),
                epochDay = today,
            )

            // ----- Assert -----
            reversed.map(::quoteKey) shouldContainExactly forward.map(::quoteKey)
            rotated.map(::quoteKey) shouldContainExactly forward.map(::quoteKey)
        }

        @Test
        fun `orders the quotes differently on a different day`() {
            // ----- Arrange -----
            val quotes = testQuotes(count = 30)

            // ----- Act -----
            val dayOne = rankForDay(
                quotes = quotes,
                epochDay = today,
            )
            val dayTwo = rankForDay(
                quotes = quotes,
                epochDay = today + 1,
            )

            // ----- Assert -----
            dayTwo.map(::quoteKey) shouldNotBe dayOne.map(::quoteKey)
        }

        @Test
        fun `keeps every quote exactly once`() {
            // ----- Arrange -----
            val quotes = testQuotes(count = 12)

            // ----- Act -----
            val ranking = rankForDay(
                quotes = quotes,
                epochDay = today,
            )

            // ----- Assert -----
            ranking.map(::quoteKey).toSet() shouldBe quotes.map(::quoteKey).toSet()
            ranking.size shouldBe quotes.size
        }

        @Test
        fun `adding a quote leaves the relative order of the others unchanged`() {
            // ----- Arrange -----
            val quotes = testQuotes(count = 15)
            val added = testQuote(
                bookId = 999,
                text = "Brand new",
            )
            val before = rankForDay(
                quotes = quotes,
                epochDay = today,
            )

            // ----- Act -----
            val after = rankForDay(
                quotes = quotes + added,
                epochDay = today,
            )

            // ----- Assert -----
            after.map(::quoteKey).filter { it != quoteKey(added) } shouldContainExactly before.map(::quoteKey)
        }

        @Test
        fun `counts a quote once when it appears under two ids`() {
            // ----- Arrange -----
            val local = testQuote(
                text = "Twice listed",
                localId = 1L,
                remoteId = null,
            )
            val synced = testQuote(
                text = "Twice listed",
                localId = 2L,
                remoteId = 40L,
            )

            // ----- Act -----
            val ranking = rankForDay(
                quotes = listOf(local, synced),
                epochDay = today,
            )

            // ----- Assert -----
            ranking.size shouldBe 1
        }

        @Test
        fun `ranks nothing when there are no quotes`() {
            // ----- Arrange -----
            val quotes = emptyList<Quote>()

            // ----- Act -----
            val ranking = rankForDay(
                quotes = quotes,
                epochDay = today,
            )

            // ----- Assert -----
            ranking shouldBe emptyList()
        }
    }

    @Nested
    inner class ResolveShown {
        private val ranking = rankForDay(
            quotes = testQuotes(count = 6),
            epochDay = today,
        )

        @Test
        fun `keeps the quote picked today`() {
            // ----- Arrange -----
            val picked = ranking[3]

            // ----- Act -----
            val shown = resolveShown(
                ranking = ranking,
                selectedQuoteId = quoteId(picked),
                selectedDay = today,
                today = today,
            )

            // ----- Assert -----
            shown shouldBe picked
        }

        @Test
        fun `falls back to rank zero when the pick is from an earlier day`() {
            // ----- Arrange -----
            val picked = ranking[3]

            // ----- Act -----
            val shown = resolveShown(
                ranking = ranking,
                selectedQuoteId = quoteId(picked),
                selectedDay = today - 1,
                today = today,
            )

            // ----- Assert -----
            shown shouldBe ranking.first()
        }

        @Test
        fun `falls back to rank zero when the picked quote is gone`() {
            // ----- Arrange -----
            val gone = testQuote(
                bookId = 404,
                text = "Deleted",
            )

            // ----- Act -----
            val shown = resolveShown(
                ranking = ranking,
                selectedQuoteId = quoteId(gone),
                selectedDay = today,
                today = today,
            )

            // ----- Assert -----
            shown shouldBe ranking.first()
        }

        @Test
        fun `shows rank zero when nothing was picked`() {
            // ----- Arrange -----
            val selectedQuoteId: String? = null
            val selectedDay: Long? = null

            // ----- Act -----
            val shown = resolveShown(
                ranking = ranking,
                selectedQuoteId = selectedQuoteId,
                selectedDay = selectedDay,
                today = today,
            )

            // ----- Assert -----
            shown shouldBe ranking.first()
        }

        @Test
        fun `shows nothing when there are no quotes`() {
            // ----- Arrange -----
            val noQuotes = emptyList<Quote>()

            // ----- Act -----
            val shown = resolveShown(
                ranking = noQuotes,
                selectedQuoteId = "gone",
                selectedDay = today,
                today = today,
            )

            // ----- Assert -----
            shown.shouldBeNull()
        }
    }

    @Nested
    inner class StepQuote {
        private val ranking = rankForDay(
            quotes = testQuotes(count = 5),
            epochDay = today,
        )

        @Test
        fun `steps forward to the next rank`() {
            // ----- Arrange -----
            val current = quoteId(ranking[1])

            // ----- Act -----
            val next = stepQuote(
                ranking = ranking,
                currentQuoteId = current,
                step = 1,
            )

            // ----- Assert -----
            next shouldBe ranking[2]
        }

        @Test
        fun `steps back to the previous rank`() {
            // ----- Arrange -----
            val current = quoteId(ranking[3])

            // ----- Act -----
            val previous = stepQuote(
                ranking = ranking,
                currentQuoteId = current,
                step = -1,
            )

            // ----- Assert -----
            previous shouldBe ranking[2]
        }

        @Test
        fun `wraps from the last rank to the first when stepping forward`() {
            // ----- Arrange -----
            val current = quoteId(ranking.last())

            // ----- Act -----
            val next = stepQuote(
                ranking = ranking,
                currentQuoteId = current,
                step = 1,
            )

            // ----- Assert -----
            next shouldBe ranking.first()
        }

        @Test
        fun `wraps from the first rank to the last when stepping back`() {
            // ----- Arrange -----
            val current = quoteId(ranking.first())

            // ----- Act -----
            val previous = stepQuote(
                ranking = ranking,
                currentQuoteId = current,
                step = -1,
            )

            // ----- Assert -----
            previous shouldBe ranking.last()
        }

        @Test
        fun `steps from rank zero when the current id is unknown`() {
            // ----- Arrange -----
            val unknownId: String? = "not in the ranking"

            // ----- Act -----
            val next = stepQuote(
                ranking = ranking,
                currentQuoteId = unknownId,
                step = 1,
            )

            // ----- Assert -----
            next shouldBe ranking[1]
        }

        @Test
        fun `stays on the only quote whichever way it steps`() {
            // ----- Arrange -----
            val only = listOf(testQuote())

            // ----- Act -----
            val forward = stepQuote(
                ranking = only,
                currentQuoteId = quoteId(only.single()),
                step = 1,
            )
            val back = stepQuote(
                ranking = only,
                currentQuoteId = quoteId(only.single()),
                step = -1,
            )

            // ----- Assert -----
            forward.shouldNotBeNull() shouldBe only.single()
            back.shouldNotBeNull() shouldBe only.single()
        }

        @Test
        fun `steps to nothing when there are no quotes`() {
            // ----- Arrange -----
            val noQuotes = emptyList<Quote>()

            // ----- Act -----
            val next = stepQuote(
                ranking = noQuotes,
                currentQuoteId = null,
                step = 1,
            )

            // ----- Assert -----
            next.shouldBeNull()
        }
    }
}

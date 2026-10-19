package nl.rhaydus.softcover.feature.widget.presentation.mapper

import android.net.Uri
import app.cash.turbine.test
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Quote
import nl.rhaydus.softcover.core.domain.model.QuotePosition
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.feature.widget.domain.model.QuoteFetchTracker
import nl.rhaydus.softcover.feature.widget.domain.model.QuoteSelection
import nl.rhaydus.softcover.feature.widget.domain.util.quoteId
import nl.rhaydus.softcover.feature.widget.domain.util.rankForDay
import nl.rhaydus.softcover.feature.widget.presentation.model.QuoteWidgetUiModel
import nl.rhaydus.softcover.feature.widget.testQuote
import nl.rhaydus.softcover.feature.widget.testQuotes
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class QuoteWidgetModelsTest {
    private val today = 20_000L

    private fun models(
        isSignedIn: Boolean = true,
        quotes: List<Quote>,
        selection: QuoteSelection? = null,
        fetchState: Flow<QuoteFetchTracker.State?> = flowOf(null),
        epochDays: Flow<Long> = flowOf(today),
        artworkUris: suspend (Quote) -> QuoteWidgetUiModel.Artwork? = { null },
    ): Flow<QuoteWidgetUiModel> = quoteWidgetModels(
        isSignedIn = flowOf(isSignedIn),
        themeConfiguration = flowOf(ThemeConfiguration()),
        quotes = flowOf(quotes),
        selection = flowOf(selection),
        fetchState = fetchState,
        artworkUris = artworkUris,
        today = epochDays,
    )

    private suspend fun Flow<QuoteWidgetUiModel>.shown(): QuoteWidgetUiModel.Content.Shown =
        toList().last().content.shouldBeInstanceOf<QuoteWidgetUiModel.Content.Shown>()

    private suspend fun Flow<QuoteWidgetUiModel>.content(): QuoteWidgetUiModel.Content =
        toList().first().content

    @Nested
    inner class Content {
        @Test
        fun `is signed out when the user is not signed in even with quotes saved`() = runTest {
            // ----- Arrange -----
            val flow = models(
                isSignedIn = false,
                quotes = testQuotes(count = 3),
            )

            // ----- Act -----
            val content = flow.content()

            // ----- Assert -----
            content shouldBe QuoteWidgetUiModel.Content.SignedOut
        }

        @Test
        fun `an empty list shows the loader while a fetch is running`() = runTest {
            // ----- Arrange -----
            val flow = models(
                quotes = emptyList(),
                fetchState = flowOf(QuoteFetchTracker.State.Running),
            )

            // ----- Act -----
            val content = flow.content()

            // ----- Assert -----
            content shouldBe QuoteWidgetUiModel.Content.Loading
        }

        @Test
        fun `an empty list with no fetch running or failed is empty`() = runTest {
            // ----- Arrange -----
            val flow = models(
                quotes = emptyList(),
                fetchState = flowOf(null),
            )

            // ----- Act -----
            val content = flow.content()

            // ----- Assert -----
            content shouldBe QuoteWidgetUiModel.Content.Empty
        }

        @Test
        fun `an empty list after a failed fetch shows the fetch failed content`() = runTest {
            // ----- Arrange -----
            val flow = models(
                quotes = emptyList(),
                fetchState = flowOf(QuoteFetchTracker.State.Failed),
            )

            // ----- Act -----
            val content = flow.content()

            // ----- Assert -----
            content shouldBe QuoteWidgetUiModel.Content.FetchFailed
        }

        @Test
        fun `quotes are shown in every fetch state`() = runTest {
            // ----- Arrange -----
            val quotes = testQuotes(count = 2)

            // ----- Act -----
            val contents = listOf(null, QuoteFetchTracker.State.Running, QuoteFetchTracker.State.Failed).map { state ->
                models(
                    quotes = quotes,
                    fetchState = flowOf(state),
                ).content()
            }

            // ----- Assert -----
            contents.forEach { it.shouldBeInstanceOf<QuoteWidgetUiModel.Content.Shown>() }
        }

        @Test
        fun `shows the quote text of the day`() = runTest {
            // ----- Arrange -----
            val quotes = testQuotes(count = 4)
            val flow = models(quotes = quotes)
            val expected = rankForDay(
                quotes = quotes,
                epochDay = today,
            ).first()

            // ----- Act -----
            val shown = flow.shown()

            // ----- Assert -----
            shown.quoteText shouldBe expected.text
            shown.bookId shouldBe expected.bookId
        }
    }

    @Nested
    inner class Eyebrow {
        @Test
        fun `reads QUOTE OF THE DAY for the first ranked quote`() = runTest {
            // ----- Arrange -----
            val flow = models(quotes = testQuotes(count = 4))

            // ----- Act -----
            val shown = flow.shown()

            // ----- Assert -----
            shown.eyebrow shouldBe "QUOTE OF THE DAY"
        }

        @Test
        fun `reads QUOTES with the place in the ranking once a quote is cycled to`() = runTest {
            // ----- Arrange -----
            val quotes = testQuotes(count = 4)
            val ranking = rankForDay(
                quotes = quotes,
                epochDay = today,
            )
            val flow = models(
                quotes = quotes,
                selection = QuoteSelection(
                    quoteId = quoteId(ranking[2]),
                    epochDay = today,
                ),
            )

            // ----- Act -----
            val shown = flow.shown()

            // ----- Assert -----
            shown.eyebrow shouldBe "QUOTES · 3/4"
            shown.quoteText shouldBe ranking[2].text
        }

        @Test
        fun `reads QUOTE OF THE DAY again when the pick is from an earlier day`() = runTest {
            // ----- Arrange -----
            val quotes = testQuotes(count = 4)
            val ranking = rankForDay(
                quotes = quotes,
                epochDay = today,
            )
            val flow = models(
                quotes = quotes,
                selection = QuoteSelection(
                    quoteId = quoteId(ranking[2]),
                    epochDay = today - 1,
                ),
            )

            // ----- Act -----
            val shown = flow.shown()

            // ----- Assert -----
            shown.eyebrow shouldBe "QUOTE OF THE DAY"
            shown.quoteText shouldBe ranking.first().text
        }
    }

    @Nested
    inner class DayRollover {
        @Test
        fun `drops a pick and shows the new quote of the day when the day changes`() = runTest {
            // ----- Arrange -----
            val quotes = testQuotes(count = 4)
            val ranking = rankForDay(
                quotes = quotes,
                epochDay = today,
            )
            val nextRanking = rankForDay(
                quotes = quotes,
                epochDay = today + 1,
            )
            val epochDays = MutableSharedFlow<Long>()
            val flow = models(
                quotes = quotes,
                selection = QuoteSelection(
                    quoteId = quoteId(ranking[2]),
                    epochDay = today,
                ),
                epochDays = epochDays,
            )

            // ----- Act & Assert -----
            flow.test {
                epochDays.emit(today)
                awaitItem()
                val before = awaitItem().content.shouldBeInstanceOf<QuoteWidgetUiModel.Content.Shown>()
                before.eyebrow shouldBe "QUOTES · 3/4"

                epochDays.emit(today + 1)
                awaitItem()
                val after = awaitItem().content.shouldBeInstanceOf<QuoteWidgetUiModel.Content.Shown>()
                after.eyebrow shouldBe "QUOTE OF THE DAY"
                after.quoteText shouldBe nextRanking.first().text
            }
        }
    }

    @Nested
    inner class Arrows {
        @Test
        fun `are hidden with a single quote`() = runTest {
            // ----- Arrange -----
            val flow = models(quotes = testQuotes(count = 1))

            // ----- Act -----
            val shown = flow.shown()

            // ----- Assert -----
            shown.hasArrows shouldBe false
        }

        @Test
        fun `are shown with more than one quote`() = runTest {
            // ----- Arrange -----
            val flow = models(quotes = testQuotes(count = 2))

            // ----- Act -----
            val shown = flow.shown()

            // ----- Assert -----
            shown.hasArrows shouldBe true
        }
    }

    @Nested
    inner class Byline {
        @Test
        fun `joins the book title and the position`() = runTest {
            // ----- Arrange -----
            val flow = models(
                quotes = listOf(
                    testQuote(
                        bookTitle = "Piranesi",
                        position = QuotePosition.pages(
                            page = 214,
                            totalPages = 437,
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val shown = flow.shown()

            // ----- Assert -----
            shown.byline shouldBe "Piranesi · p. 214"
        }

        @Test
        fun `prefers the edition title over the book title`() = runTest {
            // ----- Arrange -----
            val flow = models(
                quotes = listOf(
                    testQuote(
                        bookTitle = "Piranesi",
                        editionTitle = "Piranesi (Illustrated)",
                    ),
                ),
            )

            // ----- Act -----
            val shown = flow.shown()

            // ----- Assert -----
            shown.byline shouldBe "Piranesi (Illustrated)"
        }

        @Test
        fun `falls back to the book title when the edition title is blank`() = runTest {
            // ----- Arrange -----
            val flow = models(
                quotes = listOf(
                    testQuote(
                        bookTitle = "Piranesi",
                        editionTitle = "  ",
                    ),
                ),
            )

            // ----- Act -----
            val shown = flow.shown()

            // ----- Assert -----
            shown.byline shouldBe "Piranesi"
        }

        @Test
        fun `shows only the title when the quote has no position`() = runTest {
            // ----- Arrange -----
            val flow = models(quotes = listOf(testQuote(bookTitle = "Piranesi")))

            // ----- Act -----
            val shown = flow.shown()

            // ----- Assert -----
            shown.byline shouldBe "Piranesi"
        }

        @Test
        fun `shows only the position when the quote has no title`() = runTest {
            // ----- Arrange -----
            val flow = models(
                quotes = listOf(
                    testQuote(position = QuotePosition.Percent(percent = 53.0)),
                ),
            )

            // ----- Act -----
            val shown = flow.shown()

            // ----- Assert -----
            shown.byline shouldBe "53%"
        }

        @Test
        fun `treats a blank title as no title`() = runTest {
            // ----- Arrange -----
            val flow = models(
                quotes = listOf(
                    testQuote(
                        bookTitle = "   ",
                        position = QuotePosition.Percent(percent = 53.0),
                    ),
                ),
            )

            // ----- Act -----
            val shown = flow.shown()

            // ----- Assert -----
            shown.byline shouldBe "53%"
        }

        @Test
        fun `is null with neither a title nor a position`() = runTest {
            // ----- Arrange -----
            val flow = models(quotes = listOf(testQuote()))

            // ----- Act -----
            val shown = flow.shown()

            // ----- Assert -----
            shown.byline shouldBe null
        }
    }

    @Nested
    inner class Artwork {
        @Test
        fun `emits the shown quote without artwork first and with the resolved artwork second`() = runTest {
            // ----- Arrange -----
            val resolved = QuoteWidgetUiModel.Artwork(
                backdrop = null,
                veil = mockk<Uri>(),
                veilLift = mockk<Uri>(),
                washPrimary = mockk<Uri>(),
                washTertiary = mockk<Uri>(),
            )
            val flow = models(
                quotes = testQuotes(count = 2),
                artworkUris = { resolved },
            )

            // ----- Act -----
            val emissions = flow.toList()

            // ----- Assert -----
            emissions.size shouldBe 2
            val first = emissions[0].content.shouldBeInstanceOf<QuoteWidgetUiModel.Content.Shown>()
            val second = emissions[1].content.shouldBeInstanceOf<QuoteWidgetUiModel.Content.Shown>()
            first.artwork.shouldBeNull()
            second.artwork shouldBe resolved
            second.copy(
                artwork = null,
                imagesPending = true,
            ) shouldBe first
        }

        @Test
        fun `the text stage has its images pending and the stage after a quote without artwork does not`() = runTest {
            // ----- Arrange -----
            val flow = models(
                quotes = testQuotes(count = 2),
                artworkUris = { null },
            )

            // ----- Act -----
            val emissions = flow.toList()

            // ----- Assert -----
            emissions.size shouldBe 2
            val first = emissions[0].content.shouldBeInstanceOf<QuoteWidgetUiModel.Content.Shown>()
            val second = emissions[1].content.shouldBeInstanceOf<QuoteWidgetUiModel.Content.Shown>()
            first.imagesPending shouldBe true
            second.artwork.shouldBeNull()
            second.imagesPending shouldBe false
        }
    }
}

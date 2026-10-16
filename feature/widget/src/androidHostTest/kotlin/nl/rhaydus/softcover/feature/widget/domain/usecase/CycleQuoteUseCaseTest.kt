package nl.rhaydus.softcover.feature.widget.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Quote
import nl.rhaydus.softcover.feature.widget.domain.model.QuoteSelection
import nl.rhaydus.softcover.feature.widget.domain.repository.QuoteWidgetRepository
import nl.rhaydus.softcover.feature.widget.domain.util.quoteId
import nl.rhaydus.softcover.feature.widget.domain.util.rankForDay
import nl.rhaydus.softcover.feature.widget.testQuotes
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CycleQuoteUseCaseTest {
    private val appWidgetId = 5
    private val today = 20_000L
    private val quotes = testQuotes(count = 6)
    private val ranking = rankForDay(
        quotes = quotes,
        epochDay = today,
    )
    private val repository = mockk<QuoteWidgetRepository>()
    private val useCase = CycleQuoteUseCase(quoteWidgetRepository = repository)
    private val saved = slot<QuoteSelection>()

    private fun stubSelection(selection: QuoteSelection?) {
        coEvery {
            repository.getSelection(appWidgetId = appWidgetId)
        } returns selection
        coEvery {
            repository.saveSelection(
                appWidgetId = appWidgetId,
                selection = capture(saved),
            )
        } returns Unit
    }

    private suspend fun cycle(
        step: Int,
        allQuotes: List<Quote> = quotes,
    ): Result<Unit> = useCase(
        appWidgetId = appWidgetId,
        quotes = allQuotes,
        today = today,
        step = step,
    )

    @Nested
    inner class Invoke {
        @Test
        fun `steps forward from the quote of the day when nothing was picked`() = runTest {
            // ----- Arrange -----
            stubSelection(selection = null)

            // ----- Act -----
            val result = cycle(step = 1)

            // ----- Assert -----
            result.isSuccess shouldBe true
            saved.captured shouldBe QuoteSelection(
                quoteId = quoteId(ranking[1]),
                epochDay = today,
            )
        }

        @Test
        fun `steps forward from today's pick`() = runTest {
            // ----- Arrange -----
            stubSelection(
                selection = QuoteSelection(
                    quoteId = quoteId(ranking[2]),
                    epochDay = today,
                ),
            )

            // ----- Act -----
            cycle(step = 1)

            // ----- Assert -----
            saved.captured shouldBe QuoteSelection(
                quoteId = quoteId(ranking[3]),
                epochDay = today,
            )
        }

        @Test
        fun `steps back from today's pick`() = runTest {
            // ----- Arrange -----
            stubSelection(
                selection = QuoteSelection(
                    quoteId = quoteId(ranking[2]),
                    epochDay = today,
                ),
            )

            // ----- Act -----
            cycle(step = -1)

            // ----- Assert -----
            saved.captured shouldBe QuoteSelection(
                quoteId = quoteId(ranking[1]),
                epochDay = today,
            )
        }

        @Test
        fun `steps from rank zero when the pick is from an earlier day`() = runTest {
            // ----- Arrange -----
            stubSelection(
                selection = QuoteSelection(
                    quoteId = quoteId(ranking[4]),
                    epochDay = today - 1,
                ),
            )

            // ----- Act -----
            cycle(step = 1)

            // ----- Assert -----
            saved.captured shouldBe QuoteSelection(
                quoteId = quoteId(ranking[1]),
                epochDay = today,
            )
        }

        @Test
        fun `wraps to the last quote when stepping back from the quote of the day`() = runTest {
            // ----- Arrange -----
            stubSelection(selection = null)

            // ----- Act -----
            cycle(step = -1)

            // ----- Assert -----
            saved.captured shouldBe QuoteSelection(
                quoteId = quoteId(ranking.last()),
                epochDay = today,
            )
        }

        @Test
        fun `saves nothing when there are no quotes`() = runTest {
            // ----- Arrange -----
            stubSelection(selection = null)

            // ----- Act -----
            val result = cycle(
                step = 1,
                allQuotes = emptyList(),
            )

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 0) {
                repository.saveSelection(
                    appWidgetId = any(),
                    selection = any(),
                )
            }
        }
    }
}
